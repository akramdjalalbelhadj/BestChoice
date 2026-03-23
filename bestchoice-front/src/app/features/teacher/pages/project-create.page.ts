import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TeacherService } from '../services/teacher.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { WorkType } from '../../../core/models/enums.model';
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
            <input id="title" type="text" formControlName="title" placeholder="Ex: Développement d'une IA de recommandation" />
            @if (form.get('title')?.touched && form.get('title')?.invalid) {
              <small class="error">Le titre est requis (max 150 car.).</small>
            }
          </div>

          <div class="field">
            <label for="description">Description détaillée</label>
            <textarea id="description" formControlName="description" rows="5" placeholder="Décrivez les enjeux et le contexte..."></textarea>
          </div>
        </div>

        <div class="form-grid">
          <div class="field">
            <label for="workType">Type de travail</label>
            <select id="workType" formControlName="workType">
              @for (type of workTypes; track type) {
                <option [value]="type">{{ type }}</option>
              }
            </select>
          </div>

          <div class="field checkbox-field">
            <label class="switch">
              <input type="checkbox" formControlName="remotePossible">
              <span class="slider"></span>
            </label>
            <span>Télétravail possible</span>
          </div>
        </div>

        <div class="form-grid">
          <div class="field">
            <label for="minStudents">Min. Étudiants</label>
            <input id="minStudents" type="number" formControlName="minStudents" />
          </div>
          <div class="field">
            <label for="maxStudents">Max. Étudiants</label>
            <input id="maxStudents" type="number" formControlName="maxStudents" />
          </div>
        </div>

        <div class="form-section">
          <h3>Compétences & Mots-clés</h3>
          <p class="text-muted">Séparez les éléments par une virgule.</p>

          <div class="field">
            <label>Compétences requises (ex: Java, Angular, SQL)</label>
            <input type="text" #skillInput (keyup.enter)="addSkill(skillInput)" placeholder="Ajouter une compétence..." />
            <div class="tag-list">
              @for (s of requiredSkills(); track s) {
                <span class="tag">{{ s }} <button type="button" (click)="removeSkill(s)">×</button></span>
              }
            </div>
          </div>

          <div class="field">
            <label>Mots-clés (ex: IA, Web, Big Data)</label>
            <input type="text" #keyInput (keyup.enter)="addKeyword(keyInput)" placeholder="Ajouter un mot-clé..." />
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

  workTypes = Object.values(WorkType);

  form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.required, Validators.maxLength(3000)]],
    workType: [WorkType.DEVELOPPEMENT, Validators.required],
    remotePossible: [false],
    minStudents: [1, [Validators.required, Validators.min(1)]],
    maxStudents: [1, [Validators.required, Validators.min(1)]]
  });

  // Gestion des Skills (Set<String> au backend)
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

  // Gestion des Keywords
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

    // Construction du payload
    const request = {
      ...this.form.getRawValue(),
      requiredSkill: this.requiredSkills(),
      keyword: this.keywords()
    } as any;

    this.teacherService.createProject(user.userId, request)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => this.router.navigate(['/app/teacher/projects']),
        error: (err) => console.error('Erreur lors de la création', err)
      });
  }
}
