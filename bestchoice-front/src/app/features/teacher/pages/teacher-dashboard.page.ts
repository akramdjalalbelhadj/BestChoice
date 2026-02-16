import { Component, inject, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TeacherService } from '../services/teacher.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { finalize } from 'rxjs';
import { MatchingService } from '../../../core/services/matching.service';
import { MatchingResultResponse } from '../../matching/models/matching.model';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="dashboard-container">
      <header class="dashboard-header">
        <div class="welcome-text">
          <h1>Tableau de bord</h1>
          <p class="text-muted">Suivi des affectations et gestion des algorithmes.</p>
        </div>

        <div class="header-actions">
          <button class="btn-outline-primary" routerLink="../matching-control">
            <span class="icon">⚙️</span> Configurer Matching
          </button>

          <button class="btn-primary" routerLink="../projects/create">
            <span class="icon">＋</span> Nouveau Projet
          </button>

          <button class="btn-logout" (click)="logout()" title="Se déconnecter">
            <span class="icon">🚪</span>
          </button>
        </div>
      </header>

      <section class="kpi-grid">
        <div class="kpi-card">
          <label>Projets Actifs</label>
          <div class="value-container">
            @if (isLoading()) { <div class="skeleton kpi-val"></div> }
            @else { <span class="value">{{ projects().length }}</span> }
          </div>
        </div>
        <div class="kpi-card">
          <label>Capacité Totale</label>
          <div class="value-container">
            @if (isLoading()) { <div class="skeleton kpi-val"></div> }
            @else { <span class="value">{{ totalCapacity() }}</span> }
          </div>
        </div>
        <div class="kpi-card highlight">
          <label>Taux de Remplissage</label>
          <div class="value-container">
            @if (isLoading()) { <div class="skeleton kpi-val"></div> }
            @else { <span class="value">{{ occupancyRate() }}%</span> }
          </div>
        </div>
      </section>

      <div class="main-layout">
        <section class="card content-left">
          <div class="card-header">
            <h3>Mes Propositions Actives</h3>
            <a routerLink="../projects" class="text-link">Gérer tout</a>
          </div>

          <div class="project-list">
            @if (isLoading()) {
              @for (i of [1,2,3]; track i) { <div class="skeleton project-item-skel"></div> }
            } @else {
              @for (p of projects(); track p.id) {
                <div class="project-item">
                  <div class="item-info">
                    <h4>{{ p.title }}</h4>
                    <p class="text-xs text-muted">
                      {{ p.workType }} •
                      <strong>{{ p.assignedStudentEmails?.length || 0 }} / {{ p.maxStudents }}</strong> places
                    </p>
                  </div>
                  <div class="item-visual">
                    <div class="progress-track">
                      <div class="progress-fill"
                           [style.width.%]="((p.assignedStudentEmails?.length || 0) / p.maxStudents) * 100">
                      </div>
                    </div>
                  </div>
                </div>
              } @empty {
                <div class="empty-state">
                  <div class="empty-icon">📂</div>
                  <p>Aucune offre publiée pour le moment.</p>
                  <button class="btn-outline-sm" routerLink="../projects/create">Créer un projet</button>
                </div>
              }
            }
          </div>
        </section>

        <section class="card content-right">
          <div class="card-header">
            <h3>Top Candidats (Matching)</h3>
            <span class="badge-beta">IA</span>
          </div>
          <div class="match-stack">
            @if (isLoadingMatches()) {
              @for (i of [1,2,3,4]; track i) { <div class="skeleton match-skel"></div> }
            } @else {
              @for (match of topGlobalMatches(); track match.id) {
                <div class="match-item-pro">
                  <div class="score">{{ (match.globalScore * 100).toFixed(0) }}%</div>
                  <div class="m-info">
                    <span class="m-name">Étudiant #{{ match.studentId }}</span>
                    <span class="m-tag">Cible : Projet {{ match.projectId }}</span>
                  </div>
                </div>
              } @empty {
                <div class="empty-state-sm">
                  <p class="text-muted">Aucun score disponible. Lancez une session de matching.</p>
                </div>
              }
            }
          </div>
        </section>
      </div>
    </main>
  `,
  styles: [`
    :host { --bg: #09090b; --card: #18181b; --border: #27272a; --primary: #3b82f6; --danger: #ef4444; --text-muted: #71717a; display: block; background: var(--bg); color: #fff; min-height: 100vh; font-family: 'Inter', sans-serif; }
    .dashboard-container { max-width: 1200px; margin: 0 auto; padding: 2.5rem 1.5rem; }

    .dashboard-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 3rem; }
    .header-actions { display: flex; gap: 12px; align-items: center; }
    h1 { font-size: 1.85rem; font-weight: 800; margin: 0; letter-spacing: -0.02em; }

    .btn-logout { background: var(--border); border: 1px solid var(--border); color: var(--danger); padding: 10px; border-radius: 8px; cursor: pointer; transition: 0.2s; }
    .btn-logout:hover { background: rgba(239, 68, 68, 0.1); border-color: var(--danger); }

    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.5rem; margin-bottom: 2.5rem; }
    .kpi-card { background: var(--card); border: 1px solid var(--border); padding: 1.5rem; border-radius: 12px; }
    .kpi-card.highlight { border-color: var(--primary); background: rgba(59, 130, 246, 0.03); }
    .kpi-card label { font-size: 0.75rem; text-transform: uppercase; color: var(--text-muted); font-weight: 600; }
    .value { font-size: 2.25rem; font-weight: 800; display: block; margin-top: 8px; }

    .main-layout { display: grid; grid-template-columns: 1.6fr 1fr; gap: 2rem; }
    @media (max-width: 900px) { .main-layout { grid-template-columns: 1fr; } }
    .card { background: var(--card); border: 1px solid var(--border); border-radius: 16px; padding: 1.5rem; }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }

    .project-item { padding: 1rem 0; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; }
    .progress-track { width: 100px; height: 6px; background: #27272a; border-radius: 3px; overflow: hidden; }
    .progress-fill { height: 100%; background: var(--primary); transition: width 0.8s ease; }

    .match-item-pro { display: flex; align-items: center; gap: 12px; padding: 12px; background: rgba(255,255,255,0.03); border-radius: 10px; margin-bottom: 8px; border: 1px solid var(--border); }
    .score { width: 40px; height: 40px; border: 2px solid var(--primary); border-radius: 50%; display: grid; place-items: center; font-size: 0.75rem; font-weight: 800; color: var(--primary); }
    .m-info { display: flex; flex-direction: column; }
    .m-name { font-size: 0.9rem; font-weight: 600; }
    .m-tag { font-size: 0.7rem; color: var(--text-muted); }

    .badge-beta { font-size: 0.6rem; padding: 2px 6px; background: var(--primary); color: white; border-radius: 4px; font-weight: 900; }

    .skeleton { background: linear-gradient(90deg, #1c1c1f 25%, #27272a 50%, #1c1c1f 75%); background-size: 200% 100%; animation: loading 1.5s infinite; border-radius: 4px; }
    @keyframes loading { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    .match-skel { height: 60px; margin-bottom: 10px; }

    .btn-primary { background: var(--primary); color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; }
    .btn-outline-primary { background: rgba(59, 130, 246, 0.1); color: var(--primary); border: 1px solid rgba(59, 130, 246, 0.5); padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 8px; }
    .text-link { color: var(--primary); text-decoration: none; font-size: 0.85rem; }
    .empty-state { padding: 2rem; text-align: center; color: var(--text-muted); }
    .empty-icon { font-size: 2rem; margin-bottom: 10px; }
  `]
})
export class TeacherDashboardPage implements OnInit {
  private teacherService = inject(TeacherService);
  private matchingService = inject(MatchingService);
  private auth = inject(AuthStore);
  private router = inject(Router);

  isLoading = signal(true);
  isLoadingMatches = signal(true);

  projects = this.teacherService.projects;
  topGlobalMatches = signal<MatchingResultResponse[]>([]);

  totalCapacity = computed(() =>
    this.projects().reduce((acc, p) => acc + (p.maxStudents || 0), 0)
  );

  occupancyRate = computed(() => {
    const total = this.totalCapacity();
    if (total === 0) return 0;
    const assigned = this.projects().reduce((acc, p) => acc + (p.assignedStudentEmails?.length || 0), 0);
    return Math.round((assigned / total) * 100);
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.teacherService.loadMyProjects(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe(myProjects => {
          if (myProjects && myProjects.length > 0) {
            this.loadMatchingInsights(myProjects[0].id);
          } else {
            this.isLoadingMatches.set(false);
          }
        });
    }
  }

  private loadMatchingInsights(projectId: number) {
    this.matchingService.getResultsByProject(projectId)
      .pipe(finalize(() => this.isLoadingMatches.set(false)))
      .subscribe({
        next: (res) => this.topGlobalMatches.set(res.slice(0, 5)),
        error: (err) => {
          console.error("Erreur de récupération des matchs :", err);
          this.topGlobalMatches.set([]);
        }
      });
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
