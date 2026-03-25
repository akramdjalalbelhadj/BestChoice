import { Component, inject, OnInit, signal, ChangeDetectionStrategy, computed } from '@angular/core';
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
  private readonly fb = inject(FormBuilder);
  private readonly subjectService = inject(SubjectService);
  private readonly route = inject(ActivatedRoute);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  isSubmitting = signal(false);
  isEditMode = signal(false);
  subjectId = signal<number | null>(null);

  requiredSkills = signal<string[]>([]);
  keywords = signal<string[]>([]);
  workTypesList = Object.values(WorkType) as WorkType[];

  form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.required, Validators.maxLength(5000)]],
    objectives: [''],
    workTypes: [[] as WorkType[], [Validators.required, Validators.minLength(1)]],
    minStudents: [1, [Validators.required, Validators.min(1)]],
    maxStudents: [2, [Validators.required, Validators.min(1)]],
    credits: [3, [Validators.required]],
    semester: [1, [Validators.required]],
    academicYear: ['2025-2026', [Validators.required]]
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.subjectId.set(+id);
      this.loadSubjectData(+id);
    }
  }

  private loadSubjectData(id: number) {
    this.subjectService.getById(id).subscribe(subject => {
      this.form.patchValue({
        title: subject.title,
        description: subject.description,
        objectives: subject.objectives,
        workTypes: subject.workTypes,
        minStudents: subject.minStudents,
        maxStudents: subject.maxStudents,
        credits: subject.credits,
        semester: subject.semester,
        academicYear: subject.academicYear
      });
      this.requiredSkills.set(subject.requiredSkills);
      this.keywords.set(subject.keywords);
    });
  }

  onWorkTypeToggle(type: WorkType) {
    const current = this.form.get('workTypes')?.value || [];
    const next = current.includes(type) ? current.filter(t => t !== type) : [...current, type];
    this.form.patchValue({ workTypes: next });
    this.form.get('workTypes')?.markAsTouched();
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
  }

  removeTag(val: string, list: 'skills' | 'keywords') {
    if (list === 'skills') this.requiredSkills.update(s => s.filter(x => x !== val));
    else this.keywords.update(k => k.filter(x => x !== val));
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.isSubmitting.set(true);
    const user = this.auth.user();
    const raw = this.form.getRawValue();
    const payload = {
      title: raw.title ?? '',
      description: raw.description ?? '',
      objectives: raw.objectives ?? '',
      workTypes: raw.workTypes ?? [],
      minStudents: raw.minStudents ?? 1,
      maxStudents: raw.maxStudents ?? 2,
      credits: raw.credits ?? 3,
      semester: raw.semester ?? 1,
      academicYear: raw.academicYear ?? '2025-2026',
      requiredSkills: this.requiredSkills(),
      keywords: this.keywords()
    };

    if (this.isEditMode()) {
      const updatePayload: SubjectUpdateRequest = payload;
      this.subjectService.update(this.subjectId()!, updatePayload)
        .pipe(finalize(() => this.isSubmitting.set(false)))
        .subscribe(() => this.router.navigate(['/app/teacher/subjects']));
    } else {
      const createPayload: SubjectCreateRequest = payload;
      this.subjectService.create(user!.userId, createPayload)
        .pipe(finalize(() => this.isSubmitting.set(false)))
        .subscribe(() => this.router.navigate(['/app/teacher/subjects']));
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
