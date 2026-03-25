import { Component, inject, OnInit, signal, ChangeDetectionStrategy, computed, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { SubjectService } from '../../../subject/services/subject.service';
import { WorkType } from '../../../../core/models/enums.model';
import { SubjectCreateRequest, SubjectUpdateRequest } from '../../../subject/models/subject.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-teacher-subject-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './teacher-subject-form.page.html',
  styleUrl: './teacher-subject-form.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TeacherSubjectFormPage implements OnInit {
  private readonly fb         = inject(FormBuilder);
  private readonly subjectService = inject(SubjectService);
  private readonly route      = inject(ActivatedRoute);
  protected readonly auth     = inject(AuthStore);
  private readonly router     = inject(Router);
  private readonly cdr        = inject(ChangeDetectorRef);

  isSubmitting = signal(false);
  isEditMode   = signal(false);
  subjectId    = signal<number | null>(null);
  isDirty      = signal(false);
  errorMsg     = signal<string | null>(null);

  requiredSkills = signal<string[]>([]);
  keywords       = signal<string[]>([]);
  workTypesList  = Object.values(WorkType) as WorkType[];

  form = this.fb.group({
    title:       ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', [Validators.required, Validators.maxLength(5000)]],
    objectives:  [''],
    workTypes:   [[] as WorkType[], []],
    minStudents: [1  as number | null],
    maxStudents: [20 as number | null],
    credits:     [3  as number | null],
    semester:    [1  as number | null],
    academicYear:['2025-2026']
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    // Sync isDirty avec n'importe quelle frappe clavier, select, checkbox
    this.form.valueChanges.subscribe(() => {
      if (this.form.dirty) {
        this.isDirty.set(true);
        this.cdr.markForCheck();
      }
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.subjectId.set(+id);
      this.loadSubjectData(+id);
    }
  }

  private loadSubjectData(id: number) {
    this.subjectService.getById(id).subscribe({
      next: subject => {
        this.form.patchValue({
          title:        subject.title        ?? '',
          description:  subject.description  ?? '',
          objectives:   subject.objectives   ?? '',
          workTypes:    (subject.workTypes   as unknown as WorkType[]) ?? [],
          minStudents:  subject.minStudents  ?? 1,
          maxStudents:  subject.maxStudents  ?? 20,
          credits:      subject.credits      ?? 3,
          semester:     subject.semester     ?? 1,
          academicYear: subject.academicYear ?? '2025-2026'
        });
        this.requiredSkills.set(subject.requiredSkills ?? []);
        this.keywords.set(subject.keywords ?? []);
        this.form.markAsPristine();
        this.isDirty.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMsg.set('Impossible de charger la matière.');
        this.cdr.markForCheck();
      }
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

    // Création : titre + description obligatoires
    if (!this.isEditMode() && this.form.invalid) return;
    // Édition : sauvegarder uniquement si modifié
    if (this.isEditMode() && !this.isDirty()) return;

    const user = this.auth.user();
    if (!user) { this.errorMsg.set('Session expirée, veuillez vous reconnecter.'); return; }

    this.isSubmitting.set(true);
    const raw = this.form.getRawValue();

    const payload = {
      title:        raw.title?.trim()  ?? '',
      description:  raw.description?.trim() ?? '',
      objectives:   raw.objectives?.trim() ?? '',
      workTypes:    (raw.workTypes ?? []) as WorkType[],
      minStudents:  raw.minStudents  ?? 1,
      maxStudents:  raw.maxStudents  ?? 20,
      credits:      raw.credits      ?? 3,
      semester:     raw.semester     ?? 1,
      academicYear: raw.academicYear ?? '2025-2026',
      requiredSkills: this.requiredSkills(),
      keywords:       this.keywords()
    };

    if (this.isEditMode()) {
      const updatePayload: SubjectUpdateRequest = payload;
      this.subjectService.update(this.subjectId()!, updatePayload)
        .pipe(finalize(() => { this.isSubmitting.set(false); this.cdr.markForCheck(); }))
        .subscribe({
          next: () => {
            this.isDirty.set(false);
            this.router.navigate(['/app/teacher/subjects']);
          },
          error: (err) => {
            const msg = err?.error?.message ?? err?.message ?? 'Erreur lors de la mise à jour.';
            this.errorMsg.set(msg);
            this.cdr.markForCheck();
          }
        });
    } else {
      const createPayload: SubjectCreateRequest = payload;
      this.subjectService.create(user.userId, createPayload)
        .pipe(finalize(() => { this.isSubmitting.set(false); this.cdr.markForCheck(); }))
        .subscribe({
          next: () => this.router.navigate(['/app/teacher/subjects']),
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
