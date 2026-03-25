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
  private readonly fb = inject(FormBuilder);
  private readonly teacherService = inject(TeacherService);
  private readonly route = inject(ActivatedRoute);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  isSubmitting = signal(false);
  isEditMode = signal(false);
  projectId = signal<number | null>(null);
  isDirty = signal(false);
  errorMsg = signal<string | null>(null);

  requiredSkills = signal<string[]>([]);
  keywords = signal<string[]>([]);
  workTypesList = Object.values(WorkType) as WorkType[];

  form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.required, Validators.maxLength(5000)]],
    workTypes: [[] as WorkType[], []],
    remotePossible: [false],
    minStudents: [1 as number | null],
    maxStudents: [2 as number | null],
    credits: [6 as number | null],
    semester: [1 as number | null],
    academicYear: ['2025-2026'],
    targetProgram: ['Master Informatique']
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    // Sync isDirty signal avec les changements du form (touches clavier, selects, checkboxes)
    this.form.valueChanges.subscribe(() => {
      if (this.form.dirty) {
        this.isDirty.set(true);
        this.cdr.markForCheck();
      }
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.projectId.set(+id);
      this.loadProjectData(+id);
    }
  }

  private loadProjectData(id: number) {
    this.teacherService.getProjectById(id).subscribe(project => {
      this.form.patchValue({
        title: project.title ?? '', description: project.description ?? '',
        workTypes: project.workTypes ?? [], remotePossible: project.remotePossible ?? false,
        minStudents: project.minStudents ?? 1, maxStudents: project.maxStudents ?? 2,
        credits: project.credits ?? null, semester: project.semester ?? null,
        academicYear: project.academicYear ?? '2025-2026',
        targetProgram: project.targetProgram ?? 'Master Informatique'
      });
      this.requiredSkills.set(project.requiredSkills ?? []);
      this.keywords.set(project.keywords ?? []);
      this.form.markAsPristine();
      this.isDirty.set(false);
      this.cdr.markForCheck();
    });
  }

  onWorkTypeToggle(type: WorkType) {
    const current = this.form.get('workTypes')?.value || [];
    const next = current.includes(type) ? current.filter(t => t !== type) : [...current, type];
    this.form.patchValue({ workTypes: next });
    this.form.markAsDirty();
    this.isDirty.set(true);
    this.cdr.markForCheck();
  }

  addTag(input: HTMLInputElement, list: 'skills' | 'keywords') {
    const val = input.value.trim();
    if (!val) return;
    if (list === 'skills') {
      if (!this.requiredSkills().includes(val)) this.requiredSkills.update(s => [...s, val]);
    } else {
      if (!this.keywords().includes(val)) this.keywords.update(k => [...k, val]);
    }
    input.value = '';
    this.isDirty.set(true);
    this.form.markAsDirty();
  }

  removeTag(val: string, list: 'skills' | 'keywords') {
    if (list === 'skills') this.requiredSkills.update(s => s.filter(x => x !== val));
    else this.keywords.update(k => k.filter(x => x !== val));
    this.isDirty.set(true);
    this.form.markAsDirty();
  }

  onSubmit() {
    this.errorMsg.set(null);

    // En mode édition : on soumet dès qu'il y a une modification (isDirty)
    // En mode création : titre + description obligatoires
    if (!this.isEditMode() && this.form.invalid) return;
    if (this.isEditMode() && !this.isDirty()) return;

    const user = this.auth.user();
    if (!user) { this.errorMsg.set('Session expirée, veuillez vous reconnecter.'); return; }

    this.isSubmitting.set(true);
    const raw = this.form.getRawValue();
    const basePayload = {
      title: raw.title?.trim() ?? '',
      description: raw.description?.trim() ?? '',
      workTypes: (raw.workTypes ?? []) as WorkType[],
      remotePossible: !!raw.remotePossible,
      minStudents: raw.minStudents ?? 1,
      maxStudents: raw.maxStudents ?? 2,
      credits: raw.credits ?? 6,
      semester: raw.semester ?? 1,
      academicYear: raw.academicYear ?? '2025-2026',
      targetProgram: raw.targetProgram ?? 'Master Informatique',
      requiredSkill: this.requiredSkills(),
      keyword: this.keywords()
    };

    if (this.isEditMode()) {
      const updatePayload: ProjectUpdateRequest = {
        title: basePayload.title,
        description: basePayload.description,
        workTypes: basePayload.workTypes,
        remotePossible: basePayload.remotePossible,
        minStudents: basePayload.minStudents,
        maxStudents: basePayload.maxStudents,
        requiredSkill: basePayload.requiredSkill,
        keyword: basePayload.keyword
      };
      this.teacherService.updateProject(this.projectId()!, updatePayload)
        .pipe(finalize(() => { this.isSubmitting.set(false); this.cdr.markForCheck(); }))
        .subscribe({
          next: () => {
            this.isDirty.set(false);
            this.router.navigate(['/app/teacher/projects']);
          },
          error: (err) => {
            const msg = err?.error?.message ?? err?.message ?? 'Erreur lors de la mise à jour.';
            this.errorMsg.set(msg);
            this.cdr.markForCheck();
          }
        });
    } else {
      const createPayload: ProjectCreateRequest = basePayload;
      this.teacherService.createProject(user.userId, createPayload)
        .pipe(finalize(() => { this.isSubmitting.set(false); this.cdr.markForCheck(); }))
        .subscribe({
          next: () => this.router.navigate(['/app/teacher/projects']),
          error: (err) => {
            const msg = err?.error?.message ?? err?.message ?? 'Erreur lors de la création.';
            this.errorMsg.set(msg);
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
