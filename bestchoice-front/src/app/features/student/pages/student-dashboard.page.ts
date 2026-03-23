import { Component, inject, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { StudentService } from '../services/student.service';
import { finalize } from 'rxjs';
import { ThemeToggleComponent } from '../../../shared/theme-toggle.component';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="brand">
          <div class="logo-box">BC</div>
          <span class="brand-name">Le Bon Choix</span>
        </div>

        <nav class="nav-links">
          <a routerLink="/app/student/dashboard" routerLinkActive="active" class="menu-item">
            <span class="icon">📊</span> Dashboard
          </a>
          <a routerLink="/app/student/projects" routerLinkActive="active" class="menu-item">
            <span class="icon">🔍</span> Projets
          </a>
          <a routerLink="/app/student/preferences" routerLinkActive="active" class="menu-item">
            <span class="icon">⭐</span> Mes Choix
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
            <h1>Tableau de bord</h1>
            <p class="text-muted">{{ profile()?.program || 'Profil à compléter' }} • Master {{ profile()?.studyYear }}</p>
          </div>

          <div class="user-control">
            <div class="user-text">
              <span class="user-name">{{ auth.displayName() }}</span>
              <span class="user-role">Étudiant</span>
            </div>
            <div class="avatar">{{ initials() }}</div>
          </div>
        </header>

        <div class="dashboard-scroll">
          <div class="surgical-container">

            <section class="card status-section">
              <div class="card-header">
                <h3>Complétude du profil</h3>
                <span class="badge" [class.active]="completion() === 100">
                   {{ completion() === 100 ? 'Matching Actif' : 'Action Requise' }}
                </span>
              </div>
              <div class="progress-container">
                <div class="progress-bar">
                  <div class="progress-fill" [style.width.%]="completion()"></div>
                </div>
                <span class="percentage-text">{{ completion() }}%</span>
              </div>
              @if (completion() < 100) {
                <p class="card-hint">🎯 Complétez vos <strong>compétences</strong> pour améliorer vos scores.</p>
              }
            </section>

            <div class="kpi-row">
              <div class="kpi-card">
                <label>Vœux enregistrés</label>
                <div class="kpi-value" [class.warning]="preferenceCount() < 1">
                  {{ preferenceCount() }}<small>/10</small>
                </div>
              </div>
              <div class="kpi-card">
                <label>Compétences validées</label>
                <div class="kpi-value">{{ profile()?.skill?.length || 0 }}</div>
              </div>
            </div>

            <section class="card">
              <div class="card-header">
                <h3>Vos Recommandations IA</h3>
                <span class="text-xs text-muted">Basé sur vos skills et intérêts</span>
              </div>

              <div class="match-list">
                @for (match of topMatches(); track match.id) {
                  <div class="match-item">
                    <div class="score-pill">{{ (match.globalScore * 100).toFixed(0) }}%</div>
                    <div class="match-body">
                      <h4>Projet #{{ match.projectId }}</h4>
                      <div class="score-breakdown">
                        <span title="Compatibilité Skills">💻 {{ (match.skillsScore * 100).toFixed(0) }}%</span>
                        <span title="Compatibilité Intérêts">🌟 {{ (match.interestsScore * 100).toFixed(0) }}%</span>
                      </div>
                    </div>
                    <button class="btn-sm" [routerLink]="['/app/student/projects', match.projectId]">Voir</button>
                  </div>
                } @empty {
                  <div class="empty-state">
                    <p class="text-muted">Aucun matching n'a encore été lancé par le professeur pour votre profil.</p>
                  </div>
                }
              </div>
            </section>

          </div>
        </div>
      </main>
    </div>
  `,
  styleUrl: './student-dashboard.page.scss'
})
export class StudentDashboardPage implements OnInit {
  protected readonly auth = inject(AuthStore);
  private readonly studentService = inject(StudentService);
  private readonly router = inject(Router);

  profile = this.studentService.studentProfile;
  topMatches = this.studentService.topMatches;
  isLoading = signal(true);
  preferenceCount = signal(0);

  completion = computed(() => {
    const p = this.profile();
    if (!p) return 0;
    let score = 0;
    if (p.program) score += 25;
    if (p.studyYear) score += 25;
    if (p.skill?.length) score += 25;
    if (p.preferredWorkType) score += 25;
    return score;
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      // 1. Charger le profil
      this.studentService.loadProfile(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe(student => {
          if (student.id) {
            // 2. Charger les top matches réels du backend
            this.studentService.loadTopMatches(student.id).subscribe();
            // 3. Charger le nombre de vœux
            this.studentService.getPreferences(student.id).subscribe(prefs => {
              this.preferenceCount.set(prefs.length);
            });
          }
        });
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
