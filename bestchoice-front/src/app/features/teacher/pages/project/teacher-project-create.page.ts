import { Component, inject, OnInit, signal, ChangeDetectionStrategy, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { WorkType } from '../../../../core/models/enums.model';
import { finalize } from 'rxjs';
import { ProjectCreateRequest, ProjectUpdateRequest } from '../../../project/models/project.model';

@Component({
  selector: 'app-teacher-project-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './teacher-project-create.page.html',
  styleUrl: './teacher-project-create.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TeacherProjectFormPage implements OnInit {
  private readonly fb             = inject(FormBuilder);
  private readonly teacherService = inject(TeacherService);
  private readonly route          = inject(ActivatedRoute);
  protected readonly auth         = inject(AuthStore);
  private readonly router         = inject(Router);
  private readonly cdr            = inject(ChangeDetectorRef);

  isSubmitting = signal(false);
  isEditMode   = signal(false);
  projectId    = signal<number | null>(null);
  isDirty      = signal(false);
  errorMsg     = signal<string | null>(null);

  // ── Signaux d'erreur explicites (fiables avec OnPush) ──────────────────────
  titleError       = signal(false);
  descriptionError = signal(false);
  workTypesError   = signal(false);
  skillsError          = signal(false);
  keywordsError        = signal(false);
  maxStudentsError     = signal(false);
  skillDuplicateWarn   = signal(false);
  keywordDuplicateWarn = signal(false);

  requiredSkills = signal<string[]>([]);
  keywords       = signal<string[]>([]);
  workTypesList  = Object.values(WorkType) as WorkType[];

  form = this.fb.group({
    title:        ['', [Validators.required, Validators.maxLength(150)]],
    description:  ['', [Validators.required, Validators.maxLength(5000)]],
    workTypes:    [[] as WorkType[], []],
    remotePossible: [false],
    maxStudents:  [2  as number | null],
    academicYear: ['2025-2026'],
    targetProgram:['Master Informatique']
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    // Effacer les erreurs dès que l'utilisateur modifie le form
    this.form.get('title')!.valueChanges.subscribe(v => {
      if (v?.trim()) { this.titleError.set(false); this.cdr.markForCheck(); }
    });
    this.form.get('description')!.valueChanges.subscribe(v => {
      if (v?.trim()) { this.descriptionError.set(false); this.cdr.markForCheck(); }
    });
    this.form.get('maxStudents')!.valueChanges.subscribe(v => {
      if (v != null && v >= 1) { this.maxStudentsError.set(false); this.cdr.markForCheck(); }
    });

    // isDirty pour le mode édition
    this.form.valueChanges.subscribe(() => {
      if (this.form.dirty) { this.isDirty.set(true); this.cdr.markForCheck(); }
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.projectId.set(+id);
      this.loadProjectData(+id);
    }
  }

  private loadProjectData(id: number) {
    this.teacherService.getProjectById(id).subscribe({
      next: project => {
        this.form.patchValue({
          title:         project.title         ?? '',
          description:   project.description   ?? '',
          workTypes:     project.workTypes      ?? [],
          remotePossible:project.remotePossible ?? false,
          maxStudents:   project.maxStudents    ?? 2,
          academicYear:  project.academicYear   ?? '2025-2026',
          targetProgram: project.targetProgram  ?? 'Master Informatique'
        });
        this.requiredSkills.set(project.requiredSkills ?? []);
        this.keywords.set(project.keywords ?? []);
        this.form.markAsPristine();
        this.isDirty.set(false);
        this.cdr.markForCheck();
      },
      error: () => { this.errorMsg.set('Impossible de charger le projet.'); this.cdr.markForCheck(); }
    });
  }

  onWorkTypeToggle(type: WorkType) {
    const current = this.form.get('workTypes')?.value || [];
    const next = current.includes(type)
      ? current.filter(t => t !== type)
      : [...current, type];
    this.form.patchValue({ workTypes: next });
    this.form.markAsDirty();
    this.isDirty.set(true);
    // Effacer l'erreur workTypes si au moins un type sélectionné
    if (next.length > 0) this.workTypesError.set(false);
    this.cdr.markForCheck();
  }

  addTag(input: HTMLInputElement, list: 'skills' | 'keywords') {
    const val = input.value.trim();
    if (!val) return;
    if (list === 'skills') {
      if (!this.requiredSkills().includes(val)) {
        this.requiredSkills.update(s => [...s, val]);
        this.skillsError.set(false);
        this.skillDuplicateWarn.set(false);
      } else {
        this.skillDuplicateWarn.set(true);
      }
    } else {
      if (!this.keywords().includes(val)) {
        this.keywords.update(k => [...k, val]);
        this.keywordsError.set(false);
        this.keywordDuplicateWarn.set(false);
      } else {
        this.keywordDuplicateWarn.set(true);
      }
    }
    input.value = '';
    this.isDirty.set(true);
    this.form.markAsDirty();
    this.cdr.markForCheck();
  }

  removeTag(val: string, list: 'skills' | 'keywords') {
    if (list === 'skills') {
      this.requiredSkills.update(s => s.filter(x => x !== val));
      if (this.requiredSkills().length === 0) this.skillsError.set(true);
    } else {
      this.keywords.update(k => k.filter(x => x !== val));
      if (this.keywords().length === 0) this.keywordsError.set(true);
    }
    this.isDirty.set(true);
    this.form.markAsDirty();
    this.cdr.markForCheck();
  }

  onSubmit() {
    this.errorMsg.set(null);

    if (this.isEditMode() && !this.isDirty()) return;

    const raw = this.form.getRawValue();

    // ── Validation en création ──────────────────────────────────────────────
    if (!this.isEditMode()) {
      const titleOk      = !!raw.title?.trim();
      const descOk       = !!raw.description?.trim();
      const workOk       = (raw.workTypes?.length ?? 0) > 0;
      const skillsOk     = this.requiredSkills().length > 0;
      const keywordsOk   = this.keywords().length > 0;
      const maxStudOk    = (raw.maxStudents ?? 0) >= 1;

      this.titleError.set(!titleOk);
      this.descriptionError.set(!descOk);
      this.workTypesError.set(!workOk);
      this.skillsError.set(!skillsOk);
      this.keywordsError.set(!keywordsOk);
      this.maxStudentsError.set(!maxStudOk);
      this.cdr.markForCheck();

      if (!titleOk || !descOk || !workOk || !skillsOk || !keywordsOk || !maxStudOk) return;
    }

    // ── Validation en édition : maxStudents aussi ───────────────────────────
    if (this.isEditMode()) {
      const maxStudOk = (raw.maxStudents ?? 0) >= 1;
      this.maxStudentsError.set(!maxStudOk);
      this.cdr.markForCheck();
      if (!maxStudOk) return;
    }

    const user = this.auth.user();
    if (!user) { this.errorMsg.set('Session expirée, veuillez vous reconnecter.'); return; }

    this.isSubmitting.set(true);
    const basePayload = {
      title:         raw.title?.trim()        ?? '',
      description:   raw.description?.trim()  ?? '',
      workTypes:     (raw.workTypes           ?? []) as WorkType[],
      remotePossible:!!raw.remotePossible,
      minStudents:   1,
      maxStudents:   raw.maxStudents          ?? 2,
      credits:       6,
      semester:      1,
      academicYear:  raw.academicYear         ?? '2025-2026',
      targetProgram: raw.targetProgram        ?? 'Master Informatique',
      requiredSkill: this.requiredSkills(),
      keyword:       this.keywords()
    };

    if (this.isEditMode()) {
      const updatePayload: ProjectUpdateRequest = {
        title:          basePayload.title,
        description:    basePayload.description,
        workTypes:      basePayload.workTypes,
        remotePossible: basePayload.remotePossible,
        maxStudents:    basePayload.maxStudents,
        requiredSkill:  basePayload.requiredSkill,
        keyword:        basePayload.keyword
      };
      this.teacherService.updateProject(this.projectId()!, updatePayload)
        .pipe(finalize(() => { this.isSubmitting.set(false); this.cdr.markForCheck(); }))
        .subscribe({
          next: () => { this.isDirty.set(false); this.router.navigate(['/app/teacher/projects']); },
          error: (err) => {
            this.errorMsg.set(err?.error?.message ?? err?.message ?? 'Erreur lors de la mise à jour.');
            this.cdr.markForCheck();
          }
        });
    } else {
      this.teacherService.createProject(user.userId, basePayload as ProjectCreateRequest)
        .pipe(finalize(() => { this.isSubmitting.set(false); this.cdr.markForCheck(); }))
        .subscribe({
          next: () => this.router.navigate(['/app/teacher/projects']),
          error: (err) => {
            this.errorMsg.set(err?.error?.message ?? err?.message ?? 'Erreur lors de la création.');
            this.cdr.markForCheck();
          }
        });
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
