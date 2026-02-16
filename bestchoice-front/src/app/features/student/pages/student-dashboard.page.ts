import { Component, inject, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStore } from '../../../core/auth/auth.store';
import { StudentService } from '../services/student.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
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
  styles: [`
    :host { --bg: #09090b; --sidebar: #111113; --card: #18181b; --border: #27272a; --primary: #3b82f6; --text-dim: #71717a; display: block; height: 100vh; background: var(--bg); color: #fff; font-family: 'Inter', sans-serif; }
    .app-layout { display: flex; height: 100vh; }
    .sidebar { width: 250px; background: var(--sidebar); border-right: 1px solid var(--border); display: flex; flex-direction: column; padding: 1.5rem; }
    .nav-links { flex: 1; display: flex; flex-direction: column; gap: 4px; margin-top: 2rem; }
    .menu-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; text-decoration: none; color: var(--text-dim); border-radius: 8px; transition: 0.2s; font-size: 0.9rem; }
    .menu-item:hover, .menu-item.active { background: #27272a; color: white; }
    .menu-item.active { color: var(--primary); background: rgba(59, 130, 246, 0.1); }
    .btn-logout { width: 100%; padding: 10px; background: transparent; border: 1px solid var(--border); color: #ef4444; border-radius: 8px; cursor: pointer; text-align: left; font-weight: 600; display: flex; align-items: center; gap: 10px; }
    .main-content { flex: 1; display: flex; flex-direction: column; }
    .top-nav { height: 80px; padding: 0 2rem; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; }
    .dashboard-scroll { flex: 1; overflow-y: auto; padding: 2rem; }
    .surgical-container { max-width: 900px; margin: 0 auto; display: grid; gap: 1.5rem; }
    .card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; }
    .progress-container { display: flex; align-items: center; gap: 15px; margin: 1rem 0; }
    .progress-bar { flex: 1; height: 8px; background: #27272a; border-radius: 4px; overflow: hidden; }
    .progress-fill { height: 100%; background: var(--primary); transition: 1s ease-out; }
    .percentage-text { font-weight: 800; color: var(--primary); width: 45px; }
    .match-list { display: grid; gap: 10px; margin-top: 1rem; }
    .match-item { display: flex; align-items: center; gap: 15px; padding: 12px; background: #09090b; border: 1px solid var(--border); border-radius: 10px; }
    .score-pill { width: 50px; height: 50px; border: 2px solid var(--primary); border-radius: 50%; display: grid; place-items: center; font-size: 0.8rem; font-weight: 800; color: var(--primary); }
    .score-breakdown { display: flex; gap: 12px; margin-top: 4px; }
    .score-breakdown span { font-size: 0.7rem; color: var(--text-dim); background: #18181b; padding: 2px 6px; border-radius: 4px; }
    .btn-sm { padding: 6px 12px; background: var(--border); border: none; color: #fff; border-radius: 6px; cursor: pointer; margin-left: auto; }
  `]
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
