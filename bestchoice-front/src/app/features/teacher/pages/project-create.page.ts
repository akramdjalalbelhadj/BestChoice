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
        <a routerLink="../" class="back-link">← Retour aux projets</a>
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
  styles: [`
    :host { --bg: #09090b; --card: #18181b; --border: #27272a; --primary: #3b82f6; display: block; background: var(--bg); color: #fff; min-height: 100vh; }
    .page-container { max-width: 800px; margin: 0 auto; padding: 3rem 1.5rem; }

    .header { margin-bottom: 2rem; }
    .back-link { color: var(--primary); text-decoration: none; font-size: 0.9rem; display: block; margin-bottom: 0.5rem; }
    h1 { font-size: 2rem; font-weight: 800; margin: 0; }

    .create-card { background: var(--card); border: 1px solid var(--border); border-radius: 16px; padding: 2rem; }
    .form-section { margin-bottom: 2.5rem; }
    h3 { font-size: 1.1rem; margin-bottom: 1.25rem; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem; }

    .field { display: flex; flex-direction: column; gap: 8px; margin-bottom: 1.5rem; }
    label { font-size: 0.85rem; font-weight: 600; color: #a1a1aa; }
    input[type="text"], input[type="number"], textarea, select {
      background: #09090b; border: 1px solid var(--border); border-radius: 8px; padding: 0.75rem 1rem; color: #fff; outline: none;
    }
    input:focus, textarea:focus { border-color: var(--primary); }

    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    .checkbox-field { flex-direction: row; align-items: center; gap: 12px; margin-top: 2rem; }

    /* Tags UI */
    .tag-list { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
    .tag { background: rgba(59, 130, 246, 0.1); color: var(--primary); padding: 4px 10px; border-radius: 6px; font-size: 0.8rem; font-weight: 600; display: flex; align-items: center; gap: 6px; }
    .tag.secondary { background: #27272a; color: #f4f4f5; }
    .tag button { background: none; border: none; color: inherit; cursor: pointer; font-size: 1.1rem; padding: 0; }

    .form-footer { display: flex; justify-content: flex-end; gap: 1rem; margin-top: 2rem; padding-top: 2rem; border-top: 1px solid var(--border); }
    .btn-primary { background: var(--primary); color: #fff; border: none; padding: 12px 24px; border-radius: 8px; font-weight: 700; cursor: pointer; }
    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn-ghost { background: transparent; border: 1px solid var(--border); color: #fff; padding: 12px 24px; border-radius: 8px; cursor: pointer; }
    .error { color: #ef4444; font-size: 0.75rem; }

    /* Toggle Switch Style */
    .switch { position: relative; display: inline-block; width: 40px; height: 22px; }
    .switch input { opacity: 0; width: 0; height: 0; }
    .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #27272a; transition: .4s; border-radius: 34px; }
    .slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; }
    input:checked + .slider { background-color: var(--primary); }
    input:checked + .slider:before { transform: translateX(18px); }
  `]
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
