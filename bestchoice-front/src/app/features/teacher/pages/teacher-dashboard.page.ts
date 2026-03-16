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
            Configurer Matching
          </button>

          <button class="btn-primary" routerLink="../projects/create">
            <span class="plus">+</span> Nouveau Projet
          </button>

          <button class="btn-logout" (click)="logout()" title="Se déconnecter">
            Déconnexion
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
                  <div class="empty-icon">
                    <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
                      <rect x="8" y="16" width="48" height="40" rx="4" stroke="#d1d5db" stroke-width="2" />
                      <path d="M24 32H40" stroke="#d1d5db" stroke-width="2" stroke-linecap="round" />
                      <path d="M24 40H40" stroke="#d1d5db" stroke-width="2" stroke-linecap="round" />
                    </svg>
                  </div>
                  <p>Aucune offre publiée pour le moment.</p>
                  <button class="btn-outline-sm" routerLink="../projects/create">Créer un projet</button>
                </div>
              }
            }
          </div>
        </section>

        <section class="card content-right">
          <div class="card-header">
            <h3>Top Candidats</h3>
            <span class="badge-ai">Recommandations IA</span>
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
    :host { --bg: #f8f9fa; --card: #ffffff; --border: #e9ecef; --primary: #007bff; --danger: #dc3545; --text-muted: #6c757d; display: block; background: var(--bg); color: #212529; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif; }
    .dashboard-container { max-width: 1200px; margin: 0 auto; padding: 3rem 2rem; }

    .dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4rem; gap: 2rem; }
    .header-actions { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
    h1 { font-size: 2.5rem; font-weight: 700; margin: 0; letter-spacing: -0.02em; color: #212529; }

    /* Boutons Modernes et Simples */
    button { font-family: inherit; }
    .plus { font-size: 1.1rem; font-weight: 300; }

    .btn-primary {
      background: var(--primary);
      color: white;
      border: none;
      padding: 11px 22px;
      border-radius: 10px;
      font-weight: 600;
      font-size: 0.95rem;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 8px rgba(0, 123, 255, 0.15);
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .btn-primary:hover {
      background: #0056cc;
      box-shadow: 0 4px 12px rgba(0, 123, 255, 0.25);
      transform: translateY(-2px);
    }
    .btn-primary:active {
      transform: translateY(0);
      box-shadow: 0 2px 4px rgba(0, 123, 255, 0.15);
    }
    .btn-primary:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1), 0 2px 8px rgba(0, 123, 255, 0.15);
    }

    .btn-outline-primary {
      background: rgba(0, 123, 255, 0.08);
      color: var(--primary);
      border: 1.5px solid rgba(0, 123, 255, 0.25);
      padding: 10px 20px;
      border-radius: 10px;
      font-weight: 600;
      font-size: 0.95rem;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .btn-outline-primary:hover {
      background: rgba(0, 123, 255, 0.12);
      border-color: var(--primary);
      transform: translateY(-2px);
    }
    .btn-outline-primary:active {
      transform: translateY(0);
    }
    .btn-outline-primary:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
    }

    .btn-logout {
      background: transparent;
      border: 1.5px solid rgba(220, 53, 69, 0.3);
      color: var(--danger);
      padding: 10px 18px;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      font-size: 0.95rem;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .btn-logout:hover {
      background: rgba(220, 53, 69, 0.08);
      border-color: var(--danger);
      transform: translateY(-2px);
    }
    .btn-logout:active {
      transform: translateY(0);
    }
    .btn-logout:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
    }

    .btn-outline-sm {
      background: rgba(0, 123, 255, 0.08);
      color: var(--primary);
      border: 1.5px solid rgba(0, 123, 255, 0.25);
      padding: 10px 16px;
      border-radius: 8px;
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .btn-outline-sm:hover {
      background: rgba(0, 123, 255, 0.12);
      border-color: var(--primary);
      transform: translateY(-1px);
    }

    .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 2rem; margin-bottom: 3rem; }
    .kpi-card { background: var(--card); border: 1px solid var(--border); padding: 2rem; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
    .kpi-card.highlight { border-color: var(--primary); background: rgba(0, 123, 255, 0.02); }
    .kpi-card label { font-size: 0.875rem; text-transform: uppercase; color: var(--text-muted); font-weight: 600; }
    .value { font-size: 3rem; font-weight: 700; display: block; margin-top: 12px; color: #212529; }

    .main-layout { display: grid; grid-template-columns: 1.6fr 1fr; gap: 3rem; }
    @media (max-width: 900px) { .main-layout { grid-template-columns: 1fr; } }
    .card { background: var(--card); border: 1px solid var(--border); border-radius: 20px; padding: 2rem; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }

    .project-item { padding: 1.5rem 0; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; }
    .progress-track { width: 120px; height: 8px; background: #e9ecef; border-radius: 4px; overflow: hidden; }
    .progress-fill { height: 100%; background: var(--primary); transition: width 0.8s ease; }

    .match-item-pro { display: flex; align-items: center; gap: 16px; padding: 16px; background: #f8f9fa; border-radius: 12px; margin-bottom: 12px; border: 1px solid var(--border); }
    .score { width: 48px; height: 48px; border: 2px solid var(--primary); border-radius: 50%; display: grid; place-items: center; font-size: 0.875rem; font-weight: 700; color: var(--primary); }
    .m-info { display: flex; flex-direction: column; }
    .m-name { font-size: 1rem; font-weight: 600; color: #212529; }
    .m-tag { font-size: 0.8rem; color: var(--text-muted); }

    .badge-beta { font-size: 0.7rem; padding: 6px 10px; background: var(--primary); color: white; border-radius: 6px; font-weight: 700; }

    .badge-ai {
      font-size: 0.75rem;
      padding: 7px 12px;
      background: linear-gradient(135deg, rgba(0, 123, 255, 0.1) 0%, rgba(0, 123, 255, 0.05) 100%);
      color: var(--primary);
      border: 1px solid rgba(0, 123, 255, 0.25);
      border-radius: 8px;
      font-weight: 600;
      letter-spacing: 0.5px;
      text-transform: uppercase;
    }

    .skeleton { background: linear-gradient(90deg, #e9ecef 25%, #f8f9fa 50%, #e9ecef 75%); background-size: 200% 100%; animation: loading 1.5s infinite; border-radius: 6px; }
    @keyframes loading { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    .match-skel { height: 72px; margin-bottom: 12px; }
    .kpi-val { height: 50px; }
    .project-item-skel { height: 80px; margin-bottom: 12px; }

    .text-link { color: var(--primary); text-decoration: none; font-size: 0.9rem; font-weight: 500; transition: all 0.3s ease; }
    .text-link:hover { color: #0056cc; }

    .empty-state { padding: 3rem; text-align: center; color: var(--text-muted); }
    .empty-icon { font-size: 2.5rem; margin-bottom: 16px; }
    .welcome-text p { color: var(--text-muted); font-size: 1.1rem; }
    .project-list { margin-top: 1rem; }
    .match-stack { margin-top: 1rem; }
    .empty-state-sm { padding: 2rem; text-align: center; }
    .empty-state-sm p { color: var(--text-muted); font-size: 0.9rem; }
    .item-info h4 { color: #212529; margin: 0; }
    .item-info p { color: var(--text-muted); margin: 0.5rem 0; font-size: 0.875rem; }
    .text-xs { font-size: 0.8rem; }
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
