import { Component, inject, OnInit, signal, computed, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { StudentService } from '../../services/student.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { WorkType } from '../../../../core/models/enums.model';
import { finalize, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';

@Component({
  selector: 'app-student-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule, ReactiveFormsModule, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.Default,
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
          <div class="profile-grid" [formGroup]="profileForm">

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
                    <select formControlName="preferredWorkTypes">
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
                <label>
                  Mes Compétences
                  @if (isEditing() && allSkills().length > 0) {
                    <span class="catalog-count">({{ availableSkills().length }} disponible{{ availableSkills().length > 1 ? 's' : '' }} sur {{ allSkills().length }})</span>
                  }
                </label>
                @if (isEditing()) {
                  <div class="suggestion-container">
                    @if (allSkills().length === 0) {
                      <p class="catalog-empty">⏳ Chargement du catalogue...</p>
                    } @else if (availableSkills().length === 0) {
                      <p class="catalog-empty">✅ Vous avez déjà toutes les compétences du catalogue.</p>
                    } @else {
                      <select #skillSelect (change)="addSkill(skillSelect.value); skillSelect.value = ''">
                        <option value="">+ Ajouter une compétence du catalogue</option>
                        @for (s of availableSkills(); track s.id) {
                          <option [value]="s.name">{{ s.name }}{{ s.category ? ' (' + s.category + ')' : '' }}</option>
                        }
                      </select>
                    }
                  </div>
                }
                <div class="tag-cloud">
                  @for (s of editedSkills(); track s) {
                    <span class="tag-skill">
                      {{ s }}
                      @if (isEditing()) {
                        <button (click)="removeSkill(s)">×</button>
                      }
                    </span>
                  }
                  @if (editedSkills().length === 0) {
                    <span class="tag-empty">Aucune compétence ajoutée</span>
                  }
                </div>
              </div>

              <div class="tag-section mt-4">
                <label>
                  Centres d'intérêt
                  @if (isEditing() && allKeywords().length > 0) {
                    <span class="catalog-count">({{ availableKeywords().length }} disponible{{ availableKeywords().length > 1 ? 's' : '' }} sur {{ allKeywords().length }})</span>
                  }
                </label>
                @if (isEditing()) {
                  <div class="suggestion-container">
                    @if (allKeywords().length === 0) {
                      <p class="catalog-empty">⏳ Chargement du catalogue...</p>
                    } @else if (availableKeywords().length === 0) {
                      <p class="catalog-empty">✅ Vous avez déjà tous les mots-clés du catalogue.</p>
                    } @else {
                      <select #keySelect (change)="addInterest(keySelect.value); keySelect.value = ''">
                        <option value="">+ Ajouter un mot-clé du catalogue</option>
                        @for (k of availableKeywords(); track k.id) {
                          <option [value]="k.label">#{{ k.label }}</option>
                        }
                      </select>
                    }
                  </div>
                }
                <div class="tag-cloud">
                  @for (k of editedInterests(); track k) {
                    <span class="tag-interest">
                      #{{ k }}
                      @if (isEditing()) {
                        <button (click)="removeInterest(k)">×</button>
                      }
                    </span>
                  }
                  @if (editedInterests().length === 0) {
                    <span class="tag-empty">Aucun centre d'intérêt ajouté</span>
                  }
                </div>
              </div>
            </section>

            <section class="card full-width">
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
  private cdr = inject(ChangeDetectorRef);

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
    if (!user?.userId) return;

    this.isLoading.set(true);

    // Charger tout en parallèle : profil + catalogue compétences + catalogue mots-clés
    forkJoin({
      profile: this.studentService.loadProfile(user.userId).pipe(
        catchError(err => { console.error('Erreur chargement profil:', err); return of(null); })
      ),
      skills: this.studentService.getAllActiveSkills().pipe(
        catchError(err => { console.error('Erreur chargement compétences:', err); return of([]); })
      ),
      keywords: this.studentService.getAllActiveKeywords().pipe(
        catchError(err => { console.error('Erreur chargement mots-clés:', err); return of([]); })
      )
    })
    .pipe(finalize(() => { this.isLoading.set(false); this.cdr.detectChanges(); }))
    .subscribe(({ profile, skills, keywords }) => {
      if (skills && skills.length > 0) {
        this.allSkills.set(skills);
      }
      if (keywords && keywords.length > 0) {
        this.allKeywords.set(keywords);
      }
      if (profile) {
        this.editedSkills.set([...(profile.skill || [])]);
        this.editedInterests.set([...(profile.interestKeyword || [])]);
      }
      this.cdr.detectChanges();
    });
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

      const workTypeValue = this.profileForm.value.preferredWorkTypes;
      const request = {
        studyYear: Number(this.profileForm.value.studyYear),
        preferredWorkTypes: workTypeValue
          ? (Array.isArray(workTypeValue) ? workTypeValue : [workTypeValue])
          : [],
        skill: this.editedSkills(),
        interestKeyword: this.editedInterests(),
        githubUrl: this.profileForm.value.githubUrl || '',
        linkedinUrl: this.profileForm.value.linkedinUrl || '',
        portfolioUrl: ''
      };

      this.studentService.updateProfile(studentId, request)
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
