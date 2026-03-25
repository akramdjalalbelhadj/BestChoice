import { Component, inject, OnInit, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { StudentService } from '../../services/student.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { WorkType } from '../../../../core/models/enums.model';
import { finalize } from 'rxjs';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';

@Component({
  selector: 'app-student-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule, ReactiveFormsModule, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="brand">
          <div class="logo-box">BC</div>
          <span class="brand-name">Le Bon Choix</span>
        </div>
        <nav class="nav-links">
          <a routerLink="/app/student/dashboard" routerLinkActive="active"
             [routerLinkActiveOptions]="{exact:true}" class="menu-item">
            <span class="icon">📊</span> Dashboard
          </a>
          <a routerLink="/app/student/campaigns" routerLinkActive="active" class="menu-item">
            <span class="icon">🔍</span> Mes Campagnes
          </a>
          <a routerLink="/app/student/preferences" routerLinkActive="active" class="menu-item">
            <span class="icon">⭐</span> Mes Vœux
          </a>
          <a routerLink="/app/student/profile" routerLinkActive="active" class="menu-item">
            <span class="icon">👤</span> Mon Profil
          </a>
        </nav>
        <footer class="sidebar-footer">
          <app-theme-toggle />
          <button (click)="logout()" class="btn-logout">
            <span class="icon">🚪</span> Déconnexion
          </button>
        </footer>
      </aside>

      <main class="main-content">
        <header class="top-nav">
          <div class="header-titles">
            <h1>Mon Profil</h1>
          </div>
          <div class="user-control">
            <div class="user-text">
              <span class="user-name">{{ auth.displayName() }}</span>
              <span class="user-role">Étudiant</span>
            </div>
            <div class="avatar">{{ initials() }}</div>
          </div>
        </header>

        <div class="page-scroll">
        <div class="page-header">
          <div class="page-titles">
            <h2>Mon Profil Étudiant</h2>
            <p>Gérez vos compétences, centres d'intérêt et informations académiques.</p>
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
        </div>

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
                    <span class="work-type-badge">{{ profile()?.preferredWorkTypes }}</span>
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
        </div><!-- end page-scroll -->
      </main>
    </div>
  `,
  styleUrl: './student-profile.page.scss'
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
    preferredWorkTypes: [[] as WorkType[]],
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
        preferredWorkTypes: p.preferredWorkTypes,
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
        preferredWorkType: this.profileForm.value.preferredWorkTypes,
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
