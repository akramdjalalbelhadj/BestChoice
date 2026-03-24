import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { WorkType } from '../../../../core/models/enums.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-project-create',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page-container">
      <header class="header">
        <a routerLink="../" class="btn-retour">← Retour aux projets</a>
        <h1>Publier une nouvelle offre</h1>
      </header>

      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="create-card">
        <div class="form-section">
          <h3>Description du projet</h3>
          <div class="field">
            <label for="title">Titre du projet</label>
            <input id="title" type="text" formControlName="title" placeholder="Ex: IA de recommandation" />
          </div>

          <div class="field">
            <label for="description">Description détaillée</label>
            <textarea id="description" formControlName="description" rows="4"></textarea>
          </div>
        </div>

        <div class="form-grid">
          <div class="field">
            <label>Types de travail (multi-sélection)</label>
            <div class="checkbox-group">
              @for (type of workTypesList; track type) {
                <label class="check-label">
                  <input
                    type="checkbox"
                    [value]="type"
                    [checked]="form.get('workTypes')?.value?.includes(type)"
                    (change)="onWorkTypeToggle(type, $event)">
                  {{ type }}
                </label>
              }
            </div>
            @if (form.get('workTypes')?.invalid && form.get('workTypes')?.touched) {
              <small class="error">Sélectionnez au moins un type.</small>
            }
          </div>

          <div class="field checkbox-field">
            <label class="switch">
              <input type="checkbox" formControlName="remotePossible">
              <span class="slider"></span>
            </label>
            <span>Télétravail possible</span>
          </div>
        </div>

        <div class="form-grid tertiary">
          <div class="field">
            <label>Crédits ECTS</label>
            <input type="number" formControlName="credits" />
          </div>
          <div class="field">
            <label>Semestre</label>
            <select formControlName="semester">
              <option [value]="1">Semestre 1</option>
              <option [value]="2">Semestre 2</option>
            </select>
          </div>
          <div class="field">
            <label>Année Universitaire</label>
            <input type="text" formControlName="academicYear" placeholder="2024-2025" />
          </div>
        </div>

        <div class="form-grid">
          <div class="field">
            <label>Min. Étudiants</label>
            <input type="number" formControlName="minStudents" />
          </div>
          <div class="field">
            <label>Max. Étudiants</label>
            <input type="number" formControlName="maxStudents" />
          </div>
        </div>

        <div class="form-section">
          <h3>Compétences & Mots-clés</h3>
          <div class="field">
            <label>Compétences requises (Entrée pour ajouter)</label>
            <input type="text" #skillInput (keyup.enter)="addSkill(skillInput)" placeholder="Java, Angular..." />
            <div class="tag-list">
              @for (s of requiredSkills(); track s) {
                <span class="tag">{{ s }} <button type="button" (click)="removeSkill(s)">×</button></span>
              }
            </div>
          </div>

          <div class="field">
            <label>Mots-clés</label>
            <input type="text" #keyInput (keyup.enter)="addKeyword(keyInput)" />
            <div class="tag-list">
              @for (k of keywords(); track k) {
                <span class="tag secondary">{{ k }} <button type="button" (click)="removeKeyword(k)">×</button></span>
              }
            </div>
          </div>
        </div>

        <footer class="form-footer">
          <button type="button" routerLink="../" class="btn-ghost">Annuler</button>
          <button type="submit" class="btn-primary" [disabled]="form.invalid || isSubmitting()">
            {{ isSubmitting() ? 'Publication...' : 'Publier le projet' }}
          </button>
        </footer>
      </form>
    </div>
  `,
  styleUrl: './project-create.page.scss'
})
export class ProjectCreatePage {
  private fb = inject(FormBuilder);
  private teacherService = inject(TeacherService);
  private auth = inject(AuthStore);
  private router = inject(Router);

  isSubmitting = signal(false);
  requiredSkills = signal<string[]>([]);
  keywords = signal<string[]>([]);
  workTypesList = Object.values(WorkType) as WorkType[];

  form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.required, Validators.maxLength(3000)]],
    workTypes: [[] as WorkType[], [Validators.required, Validators.minLength(1)]],
    remotePossible: [false],
    minStudents: [1, [Validators.required, Validators.min(1)]],
    maxStudents: [2, [Validators.required, Validators.min(1)]],
    credits: [6, [Validators.required]],
    semester: [1, [Validators.required]],
    academicYear: ['2024-2025', [Validators.required]],
    targetProgram: ['Master Informatique']
  });

  onWorkTypeToggle(type: WorkType, event: any) {
    const checked = event.target.checked;
    const current = this.form.get('workTypes')?.value || [];
    if (checked) {
      this.form.patchValue({ workTypes: [...current, type] });
    } else {
      this.form.patchValue({ workTypes: current.filter(t => t !== type) });
    }
  }

  addSkill(input: HTMLInputElement) {
    const val = input.value.trim();
    if (val && !this.requiredSkills().includes(val)) {
      this.requiredSkills.update(s => [...s, val]);
      input.value = '';
    }
  }

  removeSkill(val: string) {
    this.requiredSkills.update(s => s.filter(x => x !== val));
  }

  addKeyword(input: HTMLInputElement) {
    const val = input.value.trim();
    if (val && !this.keywords().includes(val)) {
      this.keywords.update(k => [...k, val]);
      input.value = '';
    }
  }

  removeKeyword(val: string) {
    this.keywords.update(k => k.filter(x => x !== val));
  }

  onSubmit() {
    if (this.form.invalid) return;
    const user = this.auth.user();
    if (!user?.userId) return;

    this.isSubmitting.set(true);
    const request = {
      ...this.form.getRawValue(),
      requiredSkill: this.requiredSkills(),
      keyword: this.keywords()
    };

    this.teacherService.createProject(user.userId, request as any)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe(() => this.router.navigate(['/app/teacher/dashboard']));
  }
}
