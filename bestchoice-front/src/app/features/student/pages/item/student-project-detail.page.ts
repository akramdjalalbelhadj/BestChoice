import { Component, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { StudentService } from '../../services/student.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ProjectResponse } from '../../../project/models/project.model';
import { PreferenceCreateRequest } from '../../models/preference.model';
import { finalize } from 'rxjs';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';

@Component({
  selector: 'app-student-project-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ReactiveFormsModule, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="brand">BC</div>
        <nav class="nav-links">
          <a routerLink="/app/student/dashboard" routerLinkActive="active" class="menu-item">📊 Dashboard</a>
          <a routerLink="/app/student/projects" routerLinkActive="active" class="menu-item">🔍 Projets</a>
          <a routerLink="/app/student/preferences" routerLinkActive="active" class="menu-item">⭐ Mes Choix</a>
          <a routerLink="/app/student/profile" routerLinkActive="active" class="menu-item">👤 Mon Profil</a>
        </nav>
        <footer class="sidebar-footer">
          <app-theme-toggle />
          <button (click)="logout()" class="btn-logout">🚪 Déconnexion</button>
        </footer>
      </aside>

      <main class="main-content">
        @if (isLoading()) {
          <div class="loading-state">Chargement du projet...</div>
        } @else if (project()) {
          <header class="detail-header">
            <a routerLink="/app/student/projects" class="btn-retour">← Retour au catalogue</a>
            <div class="header-main">
              <h1>{{ project()?.title }}</h1>
              <span class="status-badge" [class.full]="project()?.complet">
                {{ project()?.complet ? 'Complet' : 'Places disponibles' }}
              </span>
            </div>
          </header>

          <div class="detail-grid">
            <section class="info-column">
              <div class="card description-card">
                <h3>Description</h3>
                <p>{{ project()?.description }}</p>

                <div class="meta-tags">
                  <div><strong>Type :</strong> {{ project()?.workTypes?.join(', ') }}</div>
                  <div class="meta-item"><strong>Capacité :</strong> {{ project()?.maxStudents }} étudiants</div>
                  <div class="meta-item"><strong>Télétravail :</strong> {{ project()?.remotePossible ? 'Oui' : 'Non' }}</div>
                </div>
              </div>

              <div class="card tags-card">
                <h3>Compétences requises</h3>
                <div class="tag-cloud">
                  @for (skill of project()?.requiredSkills; track skill) {
                    <span class="skill-tag">{{ skill }}</span>
                  }
                </div>

                <h3 class="mt-4">Thématiques</h3>
                <div class="tag-cloud">
                  @for (kw of project()?.keywords; track kw) {
                    <span class="keyword-tag">#{{ kw }}</span>
                  }
                </div>
              </div>
            </section>

            <aside class="action-column">
              <div class="card preference-form-card">
                <h3>Ajouter à mes vœux</h3>
                <p class="text-muted text-xs">Classez ce projet entre 1 (priorité haute) et 10.</p>

                <form [formGroup]="prefForm" (ngSubmit)="onSubmitPreference()">
                  <div class="field">
                    <label>Rang de préférence (1-10)</label>
                    <input type="number" formControlName="rank" min="1" max="10" placeholder="Ex: 1" />
                  </div>

                  <div class="field">
                    <label>Ma motivation (Optionnel)</label>
                    <textarea formControlName="motivation" rows="4" placeholder="Pourquoi ce projet vous intéresse-t-il ?"></textarea>
                  </div>

                  <button type="submit" class="btn-primary w-full" [disabled]="prefForm.invalid || isSubmitting()">
                    {{ isSubmitting() ? 'Envoi...' : 'Valider mon vœu' }}
                  </button>
                </form>
              </div>

              <div class="card teacher-card">
                <span class="label">Responsable</span>
                <div class="teacher-info">
                  <div class="avatar-sm">{{ project()?.teacherName?.charAt(0) }}</div>
                  <span>{{ project()?.teacherName }}</span>
                </div>
              </div>
            </aside>
          </div>
        }
      </main>
    </div>
  `,
  styleUrl: './student-project-detail.page.scss'
})
export class StudentProjectDetailPage implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private studentService = inject(StudentService);
  protected auth = inject(AuthStore);

  project = signal<ProjectResponse | null>(null);
  isLoading = signal(true);
  isSubmitting = signal(false);

  prefForm = this.fb.group({
    rank: [1, [Validators.required, Validators.min(1), Validators.max(10)]],
    motivation: ['', [Validators.maxLength(1000)]]
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    const user = this.auth.user();

    // 1. Charger le projet
    if (id) {
      this.studentService.getProjectById(+id)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe(res => this.project.set(res));
    }

    // 2. SÉCURITÉ : Charger le profil étudiant s'il est manquant (cas du refresh page)
    if (user?.userId && !this.studentService.studentProfile()) {
      this.studentService.loadProfile(user.userId).subscribe();
    }
  }

  onSubmitPreference() {
    const proj = this.project();
    const student = this.studentService.studentProfile();

    // Debug Log pour voir où ça bloque
    console.log('Tentative de soumission:', {
      formValid: this.prefForm.valid,
      projectExist: !!proj,
      studentExist: !!student
    });

    if (this.prefForm.valid && proj && student) {
      this.isSubmitting.set(true);

      const request: PreferenceCreateRequest = {
        studentId: student.id, // ID de l'entité Student (pas du User)
        projectId: proj.id,
        rank: Number(this.prefForm.value.rank), // Forcer le type Number
        motivation: this.prefForm.value.motivation || '',
        comment: '' // Optionnel selon ton DTO
      };

      this.studentService.submitPreference(request)
        .pipe(finalize(() => this.isSubmitting.set(false)))
        .subscribe({
          next: () => {
            alert('Vœu enregistré avec succès !');
            this.router.navigate(['/app/student/preferences']);
          },
          error: (err) => {
            console.error('Erreur Backend:', err);
            // Gestion des erreurs spécifiques (Rang déjà utilisé, Projet déjà choisi)
            const msg = err.error?.message || 'Erreur : rang déjà utilisé ou projet déjà dans vos vœux.';
            alert(msg);
          }
        });
    } else if (!student) {
      alert("Erreur : Profil étudiant non chargé. Veuillez réessayer.");
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
