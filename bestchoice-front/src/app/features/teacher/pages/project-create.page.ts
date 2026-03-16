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
    :host { --bg: #f8f9fa; --card: #ffffff; --border: #e9ecef; --primary: #007bff; --danger: #dc3545; --text-muted: #6c757d; display: block; background: var(--bg); color: #212529; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif; }

    .page-container { max-width: 800px; margin: 0 auto; padding: 3rem 1.5rem; }

    .header { margin-bottom: 3rem; }
    .back-link { color: var(--primary); text-decoration: none; font-size: 0.95rem; display: block; margin-bottom: 1rem; font-weight: 500; transition: all 0.3s ease; }
    .back-link:hover { color: #0056cc; }
    h1 { font-size: 2.5rem; font-weight: 700; margin: 0; letter-spacing: -0.02em; color: #212529; }

    .create-card { background: var(--card); border: 1px solid var(--border); border-radius: 20px; padding: 2.5rem; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }

    .form-section { margin-bottom: 3rem; }
    h3 { font-size: 1.2rem; font-weight: 700; margin-bottom: 1.5rem; color: #212529; }
    .text-muted { color: var(--text-muted); font-size: 0.9rem; }

    .field { display: flex; flex-direction: column; gap: 10px; margin-bottom: 1.5rem; }
    label { font-size: 0.9rem; font-weight: 600; color: #212529; }
    input[type="text"], input[type="number"], textarea, select {
      background: #f8f9fa; border: 1.5px solid var(--border); border-radius: 10px; padding: 12px 14px; color: #212529; outline: none; font-size: 0.95rem; transition: all 0.3s ease;
    }
    input::placeholder, textarea::placeholder { color: var(--text-muted); }
    input:focus, textarea:focus, select:focus { border-color: var(--primary); background: #ffffff; box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1); }

    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; }
    @media (max-width: 600px) { .form-grid { grid-template-columns: 1fr; } }
    .checkbox-field { flex-direction: row; align-items: center; gap: 12px; margin-bottom: 1.5rem; }
    .checkbox-field label { margin: 0; }
    .checkbox-field span { font-size: 0.95rem; font-weight: 500; }

    /* Tags UI */
    .tag-list { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 12px; }
    .tag { background: rgba(0, 123, 255, 0.08); color: var(--primary); padding: 8px 12px; border-radius: 8px; font-size: 0.85rem; font-weight: 600; display: inline-flex; align-items: center; gap: 8px; border: 1px solid rgba(0, 123, 255, 0.25); }
    .tag.secondary { background: #f8f9fa; color: #212529; border-color: var(--border); }
    .tag button { background: none; border: none; color: inherit; cursor: pointer; font-size: 1.2rem; padding: 0; line-height: 1; transition: all 0.2s ease; }
    .tag button:hover { transform: scale(1.3); }

    .form-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 2.5rem; padding-top: 2rem; border-top: 1px solid var(--border); }

    .btn-primary {
      background: var(--primary);
      color: white;
      border: none;
      padding: 12px 24px;
      border-radius: 10px;
      font-weight: 600;
      font-size: 0.95rem;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 8px rgba(0, 123, 255, 0.15);
    }
    .btn-primary:hover:not(:disabled) {
      background: #0056cc;
      box-shadow: 0 4px 12px rgba(0, 123, 255, 0.25);
      transform: translateY(-2px);
    }
    .btn-primary:active:not(:disabled) {
      transform: translateY(0);
      box-shadow: 0 2px 4px rgba(0, 123, 255, 0.15);
    }
    .btn-primary:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
    .btn-primary:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
    }

    .btn-ghost {
      background: transparent;
      border: 1.5px solid var(--border);
      color: #212529;
      padding: 11px 22px;
      border-radius: 10px;
      font-weight: 600;
      font-size: 0.95rem;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .btn-ghost:hover {
      background: #f8f9fa;
      border-color: #d1d5db;
      transform: translateY(-2px);
    }
    .btn-ghost:active {
      transform: translateY(0);
    }
    .btn-ghost:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
    }

    .error { color: var(--danger); font-size: 0.8rem; font-weight: 500; }

    /* Toggle Switch Style */
    .switch { position: relative; display: inline-block; width: 48px; height: 26px; }
    .switch input { opacity: 0; width: 0; height: 0; }
    .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #e9ecef; transition: 0.4s cubic-bezier(0.4, 0, 0.2, 1); border-radius: 34px; border: 1px solid var(--border); }
    .slider:before { position: absolute; content: ""; height: 20px; width: 20px; left: 3px; bottom: 2px; background-color: white; transition: 0.4s cubic-bezier(0.4, 0, 0.2, 1); border-radius: 50%; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }
    input:checked + .slider { background-color: var(--primary); border-color: var(--primary); }
    input:checked + .slider:before { transform: translateX(22px); }
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
