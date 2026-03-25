import { Component, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { WorkType } from '../../../../core/models/enums.model';
import { finalize } from 'rxjs';
import {ProjectCreateRequest, ProjectUpdateRequest} from '../../../project/models/project.model';

@Component({
  selector: 'app-teacher-project-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './teacher-project-create.page.html',
  styleUrl: './teacher-project-create.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TeacherProjectFormPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly teacherService = inject(TeacherService);
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  // Signaux d'état
  isSubmitting = signal(false);
  isEditMode = signal(false);
  projectId = signal<number | null>(null);

  // Données de formulaire
  requiredSkills = signal<string[]>([]);
  keywords = signal<string[]>([]);
  workTypesList = Object.values(WorkType) as WorkType[];

  form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.required, Validators.maxLength(5000)]],
    workTypes: [[] as WorkType[], [Validators.required, Validators.minLength(1)]],
    remotePossible: [false],
    minStudents: [1, [Validators.required, Validators.min(1)]],
    maxStudents: [2, [Validators.required, Validators.min(1)]],
    credits: [6, [Validators.required]],
    semester: [1, [Validators.required]],
    academicYear: ['2025-2026', [Validators.required]],
    targetProgram: ['Master Informatique']
  });

  ngOnInit() {
    // Détection du mode : si un ID est dans l'URL, c'est une édition
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
        title: project.title,
        description: project.description,
        workTypes: project.workTypes,
        remotePossible: project.remotePossible,
        minStudents: project.minStudents,
        maxStudents: project.maxStudents,
        credits: project.credits,
        semester: project.semester,
        academicYear: project.academicYear,
        targetProgram: project.targetProgram
      });
      this.requiredSkills.set(project.requiredSkills);
      this.keywords.set(project.keywords);
    });
  }

  // --- LOGIQUE DES INPUTS ---

  onWorkTypeToggle(type: WorkType) {
    const current = this.form.get('workTypes')?.value || [];
    const next = current.includes(type)
      ? current.filter(t => t !== type)
      : [...current, type];
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

  // --- SOUMISSION ---

  onSubmit() {
    if (this.form.invalid) return;
    this.isSubmitting.set(true);

    const user = this.auth.user();
    const raw = this.form.getRawValue();

    // On prépare le payload de base pour la création
    const basePayload = {
      title: raw.title ?? '',
      description: raw.description ?? '',
      workTypes: raw.workTypes ?? [],
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
      // Cas MISE À JOUR : On ne garde que les champs de ProjectUpdateRequest
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
        .pipe(finalize(() => this.isSubmitting.set(false)))
        .subscribe(() => this.router.navigate(['/app/teacher/projects', this.projectId()]));

    } else {
      const createPayload: ProjectCreateRequest = basePayload;

      this.teacherService.createProject(user!.userId, createPayload)
        .pipe(finalize(() => this.isSubmitting.set(false)))
        .subscribe(() => this.router.navigate(['/app/teacher/dashboard']));
    }
  }
}
