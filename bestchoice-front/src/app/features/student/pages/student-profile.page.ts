import { Component, inject, OnInit, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { StudentService } from '../services/student.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { WorkType } from '../../../core/models/enums.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-student-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="brand">BC</div>
        <nav class="nav-links">
          <a routerLink="/app/student/dashboard" class="menu-item">📊 Dashboard</a>
          <a routerLink="/app/student/projects" class="menu-item">🔍 Projets</a>
          <a routerLink="/app/student/preferences" class="menu-item">⭐ Mes Choix</a>
          <a routerLink="/app/student/profile" class="menu-item active">👤 Mon Profil</a>
        </nav>
        <footer class="sidebar-footer">
          <button (click)="logout()" class="btn-logout">🚪 Déconnexion</button>
        </footer>
      </aside>

      <main class="main-content">
        <header class="profile-header">
          <div class="header-titles">
            <a routerLink="../dashboard" class="back-link">← Retour au dashboard</a>
            <h1>Mon Profil Étudiant</h1>
          </div>

          @if (!isEditing()) {
            <button class="btn-primary" (click)="enableEdit()">Modifier mon profil</button>
          } @else {
            <div class="edit-actions">
              <button class="btn-ghost" (click)="isEditing.set(false)">Annuler</button>
              <button class="btn-save" (click)="saveProfile()" [disabled]="isSaving()">
                {{ isSaving() ? 'Enregistrement...' : 'Enregistrer' }}
              </button>
            </div>
          }
        </header>

        @if (isLoading()) {
          <div class="loading-state">Chargement de vos données...</div>
        } @else {
          <div class="profile-grid">

            <section class="card">
              <div class="user-identity">
                <div class="avatar-lg">{{ initials() }}</div>
                <div>
                  <h2>{{ profile()?.firstName }} {{ profile()?.lastName }}</h2>
                  <p class="text-muted">{{ profile()?.email }}</p>
                </div>
              </div>

              <div class="info-list" [formGroup]="profileForm">
                <div class="info-item">
                  <label>Formation (Fixe)</label>
                  <span class="static-val">{{ profile()?.program }}</span>
                </div>

                <div class="info-item">
                  <label>Année d'étude (Master)</label>
                  @if (!isEditing()) {
                    <span class="static-val">Master {{ profile()?.studyYear }}</span>
                  } @else {
                    <input type="number" formControlName="studyYear" min="1" max="8">
                  }
                </div>

                <div class="info-item">
                  <label>Type de travail préféré</label>
                  @if (!isEditing()) {
                    <span class="work-type-badge">{{ profile()?.preferredWorkType }}</span>
                  } @else {
                    <select formControlName="preferredWorkType">
                      @for (type of workTypes; track type) {
                        <option [value]="type">{{ type }}</option>
                      }
                    </select>
                  }
                </div>
              </div>
            </section>

            <section class="card">
              <h3>Compétences & Intérêts</h3>
              <p class="text-xs text-muted mb-4">Utilisez le catalogue pour garantir votre éligibilité au matching.</p>

              <div class="tag-section">
                <label>Mes Compétences</label>
                @if (isEditing()) {
                  <div class="suggestion-container">
                    <select #skillSelect (change)="addSkill(skillSelect.value); skillSelect.value = ''">
                      <option value="">+ Ajouter une compétence du catalogue</option>
                      @for (s of availableSkills(); track s.id) {
                        <option [value]="s.name">{{ s.name }} ({{ s.category }})</option>
                      }
                    </select>
                  </div>
                }
                <div class="tag-cloud">
                  @for (s of editedSkills(); track s) {
                    <span class="tag-skill">
                      {{ s }}
                      <button *ngIf="isEditing()" (click)="removeSkill(s)">×</button>
                    </span>
                  }
                </div>
              </div>

              <div class="tag-section mt-4">
                <label>Centres d'intérêt</label>
                @if (isEditing()) {
                  <div class="suggestion-container">
                    <select #keySelect (change)="addInterest(keySelect.value); keySelect.value = ''">
                      <option value="">+ Ajouter un mot-clé du catalogue</option>
                      @for (k of availableKeywords(); track k.id) {
                        <option [value]="k.label">#{{ k.label }}</option>
                      }
                    </select>
                  </div>
                }
                <div class="tag-cloud">
                  @for (k of editedInterests(); track k) {
                    <span class="tag-interest">
                      #{{ k }}
                      <button *ngIf="isEditing()" (click)="removeInterest(k)">×</button>
                    </span>
                  }
                </div>
              </div>
            </section>

            <section class="card full-width" [formGroup]="profileForm">
              <h3>Présence en ligne</h3>
              <div class="links-edit-grid">
                <div class="field">
                  <label>URL GitHub</label>
                  <input type="text" formControlName="githubUrl" [readonly]="!isEditing()" placeholder="https://github.com/...">
                </div>
                <div class="field">
                  <label>URL LinkedIn</label>
                  <input type="text" formControlName="linkedinUrl" [readonly]="!isEditing()" placeholder="https://linkedin.com/in/...">
                </div>
              </div>
            </section>
          </div>
        }
      </main>
    </div>
  `,
  styles: [`
    .app-layout { display: flex; height: 100vh; background: #09090b; color: #fff; font-family: 'Inter', sans-serif; }
    .sidebar { width: 250px; background: #111113; border-right: 1px solid #27272a; padding: 1.5rem; display: flex; flex-direction: column; }
    .main-content { flex: 1; padding: 3rem; overflow-y: auto; }

    .profile-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2.5rem; }
    .back-link { color: #3b82f6; text-decoration: none; font-size: 0.85rem; margin-bottom: 8px; display: block; }
    h1 { font-size: 2.25rem; font-weight: 800; letter-spacing: -0.02em; }

    .profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; max-width: 1100px; }
    .full-width { grid-column: 1 / -1; }
    .card { background: #18181b; border: 1px solid #27272a; border-radius: 16px; padding: 2rem; }

    .info-list { display: grid; gap: 1.25rem; }
    label { display: block; font-size: 0.75rem; text-transform: uppercase; color: #71717a; font-weight: 700; margin-bottom: 6px; }
    .static-val { font-size: 1rem; font-weight: 500; color: #e4e4e7; }

    input, select { width: 100%; background: #09090b; border: 1px solid #27272a; border-radius: 8px; padding: 10px; color: #fff; outline: none; }
    input:focus, select:focus { border-color: #3b82f6; }

    .suggestion-container { margin-bottom: 12px; }
    .suggestion-container select { border: 1px dashed #3b82f6; color: #3b82f6; font-weight: 600; cursor: pointer; }

    .tag-cloud { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
    .tag-skill, .tag-interest { display: flex; align-items: center; gap: 6px; padding: 6px 12px; border-radius: 8px; font-size: 0.85rem; font-weight: 600; }
    .tag-skill { background: rgba(59, 130, 246, 0.1); color: #3b82f6; border: 1px solid rgba(59, 130, 246, 0.2); }
    .tag-interest { background: #27272a; color: #e4e4e7; }
    .tag-skill button, .tag-interest button { background: none; border: none; color: inherit; cursor: pointer; font-size: 1.1rem; }

    .work-type-badge { display: inline-block; background: #3b82f6; color: white; padding: 4px 12px; border-radius: 6px; font-size: 0.75rem; font-weight: 800; }
    .edit-actions { display: flex; gap: 10px; }
    .btn-save { background: #22c55e; color: #fff; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 700; cursor: pointer; }
    .btn-primary { background: #3b82f6; color: #fff; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 700; cursor: pointer; }
    .btn-ghost { background: transparent; border: 1px solid #27272a; color: #fff; padding: 10px 20px; border-radius: 8px; cursor: pointer; }
    .btn-logout { width: 100%; padding: 10px; background: transparent; border: 1px solid #27272a; color: #ef4444; border-radius: 8px; cursor: pointer; margin-top: auto; }

    .links-edit-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; }
    .loading-state { padding: 3rem; text-align: center; color: #71717a; }
    .mt-4 { margin-top: 1.5rem; }
  `]
})
export class StudentProfilePage implements OnInit {
  private fb = inject(FormBuilder);
  protected auth = inject(AuthStore);
  private studentService = inject(StudentService);
  private router = inject(Router);

  isLoading = signal(true);
  isEditing = signal(false);
  isSaving = signal(false);

  // Catalogues complets du back
  allSkills = signal<any[]>([]);
  allKeywords = signal<any[]>([]);

  profile = this.studentService.studentProfile;
  editedSkills = signal<string[]>([]);
  editedInterests = signal<string[]>([]);
  workTypes = Object.values(WorkType);

  // Filtres intelligents : Catalogue moins ce que je possède déjà
  availableSkills = computed(() => {
    const mine = this.editedSkills();
    return this.allSkills().filter(s => !mine.includes(s.name));
  });

  availableKeywords = computed(() => {
    const mine = this.editedInterests();
    return this.allKeywords().filter(k => !mine.includes(k.label));
  });

  profileForm = this.fb.group({
    studyYear: [1, [Validators.required, Validators.min(1), Validators.max(8)]],
    preferredWorkType: [WorkType.DEVELOPPEMENT],
    githubUrl: [''],
    linkedinUrl: ['']
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    this.loadInitialData();
  }

  loadInitialData() {
    const user = this.auth.user();
    if (user?.userId) {
      // 1. Charger catalogues
      this.studentService.getAllActiveSkills().subscribe(res => this.allSkills.set(res));
      this.studentService.getAllActiveKeywords().subscribe(res => this.allKeywords.set(res));

      // 2. Charger profil
      this.studentService.loadProfile(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe(res => {
          this.editedSkills.set([...(res.skill || [])]);
          this.editedInterests.set([...(res.interestKeyword || [])]);
        });
    }
  }

  enableEdit() {
    const p = this.profile();
    if (p) {
      this.profileForm.patchValue({
        studyYear: p.studyYear,
        preferredWorkType: p.preferredWorkType,
        githubUrl: p.githubUrl,
        linkedinUrl: p.linkedinUrl
      });
      this.isEditing.set(true);
    }
  }

  addSkill(name: string) {
    if (name && !this.editedSkills().includes(name)) {
      this.editedSkills.update(s => [...s, name]);
    }
  }

  removeSkill(name: string) {
    this.editedSkills.update(s => s.filter(x => x !== name));
  }

  addInterest(label: string) {
    if (label && !this.editedInterests().includes(label)) {
      this.editedInterests.update(i => [...i, label]);
    }
  }

  removeInterest(label: string) {
    this.editedInterests.update(i => i.filter(x => x !== label));
  }

  saveProfile() {
    const studentId = this.profile()?.id;
    if (studentId && this.profileForm.valid) {
      this.isSaving.set(true);

      const request = {
        studyYear: Number(this.profileForm.value.studyYear),
        preferredWorkType: this.profileForm.value.preferredWorkType,
        skill: this.editedSkills(),
        interestKeyword: this.editedInterests(),
        githubUrl: this.profileForm.value.githubUrl || '',
        linkedinUrl: this.profileForm.value.linkedinUrl || '',
        portfolioUrl: '' // Obligatoire dans ton DTO
      };

      this.studentService.updateProfile(studentId, request as any)
        .pipe(finalize(() => {
          this.isSaving.set(false);
          this.isEditing.set(false);
        }))
        .subscribe({
          next: () => alert('Profil mis à jour !'),
          error: (err) => alert('Erreur lors de la mise à jour. Vérifiez les IDs.')
        });
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
