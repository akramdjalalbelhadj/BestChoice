import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TeacherService } from '../services/teacher.service';
import { MatchingService } from '../../../core/services/matching.service';
import { ProjectResponse } from '../../project/models/project.model';
import { MatchingResultResponse } from '../../matching/models/matching.model';
import { finalize } from 'rxjs';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="detail-container">
      <header class="detail-header">
        <a routerLink="/app/teacher/dashboard" class="back-link">Retour au dashboard</a>
        <div class="header-main">
          <h1>{{ project()?.title || 'Chargement...' }}</h1>
          @if (project()) {
            <span class="badge-status" [class.active]="project()?.active">
              {{ project()?.active ? 'Publié' : 'Brouillon' }}
            </span>
          }
        </div>
      </header>

      <div class="detail-grid">
        <section class="card">
          <h3>Détails de la proposition</h3>
          <div class="stats-row">
            <div class="stat-item">
              <label>Capacité</label>
              <div class="stat-val">{{ project()?.maxStudents }} élèves</div>
            </div>
            <div class="stat-item">
              <label>Type</label>
              <div class="stat-val">{{ project()?.workType }}</div>
            </div>
          </div>

          <div class="description-box">
            <label>Description</label>
            <p>{{ project()?.description }}</p>
          </div>

          <div class="skills-box mt-4">
            <label>Compétences recherchées</label>
            <div class="tag-cloud">
              @for (s of project()?.requiredSkills; track s) {
                <span class="tag-skill">{{ s }}</span>
              }
            </div>
          </div>
        </section>

        <section class="card matching-card">
          <div class="card-header">
            <h3>Candidats Recommandés</h3>
            <button class="btn-refresh" (click)="loadCandidates()" [disabled]="isLoadingMatches()">
              {{ isLoadingMatches() ? 'Calcul en cours...' : 'Actualiser' }}
            </button>
          </div>

          <div class="candidates-list">
            @if (isLoadingMatches()) {
              <div class="skeleton-list">Analyse des scores en cours...</div>
            } @else {
              @for (m of candidates(); track m.id) {
                <div class="candidate-item">
                  <div class="match-score">{{ (m.globalScore * 100).toFixed(0) }}%</div>
                  <div class="cand-info">
                    <span class="cand-name">Étudiant #{{ m.studentId }}</span>
                    <div class="cand-details">
                      <span>Skills: {{ (m.skillsScore * 100).toFixed(0) }}%</span>
                      <span>Intérêts: {{ (m.interestsScore * 100).toFixed(0) }}%</span>
                    </div>
                  </div>
                  <button class="btn-view" [routerLink]="['/app/teacher/students', m.studentId]">Profil</button>
                </div>
              } @empty {
                <div class="empty-matches">
                  <p>Aucun résultat. Lancez un matching global pour voir les candidats compatibles.</p>
                  <button class="btn-primary-sm" routerLink="/app/teacher/matching-control">Configurer le Matching</button>
                </div>
              }
            }
          </div>
        </section>
      </div>
    </div>
  `,
  styles: [`
    :host { --bg: #f8f9fa; --card: #ffffff; --border: #e9ecef; --primary: #007bff; --danger: #dc3545; --text-muted: #6c757d; display: block; background: var(--bg); color: #212529; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif; }

    .detail-container { padding: 3rem 2rem; max-width: 1200px; margin: 0 auto; }

    /* Header */
    .detail-header { margin-bottom: 3rem; }
    .back-link { color: var(--primary); text-decoration: none; font-size: 0.95rem; font-weight: 500; display: block; margin-bottom: 1.5rem; transition: all 0.3s ease; }
    .back-link:hover { color: #0056cc; }
    .header-main { display: flex; align-items: center; gap: 16px; }
    h1 { font-size: 2.5rem; font-weight: 700; margin: 0; letter-spacing: -0.02em; color: #212529; }

    /* Badges */
    .badge-status { font-size: 0.75rem; font-weight: 700; padding: 7px 12px; border-radius: 8px; background: rgba(107, 114, 128, 0.1); color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; border: 1px solid rgba(107, 114, 128, 0.25); }
    .badge-status.active { background: rgba(0, 123, 255, 0.1); color: var(--primary); border-color: var(--primary); }

    /* Grid */
    .detail-grid { display: grid; grid-template-columns: 1fr 1.2fr; gap: 2rem; }
    @media (max-width: 900px) { .detail-grid { grid-template-columns: 1fr; } }

    /* Cards */
    .card { background: var(--card); border: 1px solid var(--border); border-radius: 20px; padding: 2rem; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }
    .card h3 { font-size: 1.2rem; font-weight: 700; margin-bottom: 1.5rem; color: #212529; }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }

    /* Stats */
    .stats-row { display: flex; gap: 40px; margin: 1.5rem 0; }
    .stat-item { display: flex; flex-direction: column; gap: 8px; }
    label { font-size: 0.85rem; color: var(--text-muted); text-transform: uppercase; font-weight: 600; letter-spacing: 0.5px; }
    .stat-val { font-size: 1.4rem; font-weight: 700; color: var(--primary); }

    /* Description Box */
    .description-box { padding: 1.5rem; background: #f8f9fa; border-radius: 12px; border: 1px solid var(--border); }
    .description-box p { color: #212529; line-height: 1.6; margin: 0.5rem 0 0 0; }

    /* Skills Box */
    .skills-box { margin-top: 2rem; }
    .tag-cloud { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 12px; }
    .tag-skill { background: rgba(0, 123, 255, 0.08); color: var(--primary); padding: 8px 12px; border-radius: 8px; font-size: 0.85rem; border: 1px solid rgba(0, 123, 255, 0.25); font-weight: 600; }

    /* Candidates List */
    .candidates-list { display: flex; flex-direction: column; gap: 12px; }
    .candidate-item { display: flex; align-items: center; gap: 16px; padding: 16px; background: #f8f9fa; border: 1px solid var(--border); border-radius: 12px; transition: all 0.3s ease; }
    .candidate-item:hover { background: #ffffff; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }

    .match-score { width: 48px; height: 48px; border: 2px solid var(--primary); border-radius: 50%; display: grid; place-items: center; font-weight: 700; font-size: 0.9rem; color: var(--primary); flex-shrink: 0; }
    .cand-info { flex: 1; display: flex; flex-direction: column; gap: 6px; }
    .cand-name { font-weight: 700; font-size: 0.95rem; color: #212529; }
    .cand-details { display: flex; gap: 16px; font-size: 0.85rem; color: var(--text-muted); }

    /* Buttons */
    .btn-refresh {
      background: rgba(0, 123, 255, 0.08);
      border: 1.5px solid rgba(0, 123, 255, 0.25);
      color: var(--primary);
      padding: 10px 18px;
      border-radius: 10px;
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .btn-refresh:hover:not(:disabled) {
      background: rgba(0, 123, 255, 0.12);
      border-color: var(--primary);
      transform: translateY(-2px);
    }
    .btn-refresh:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .btn-view {
      background: #f8f9fa;
      border: 1.5px solid var(--border);
      color: var(--primary);
      padding: 8px 14px;
      border-radius: 8px;
      cursor: pointer;
      font-size: 0.85rem;
      font-weight: 600;
      transition: all 0.3s ease;
    }
    .btn-view:hover {
      background: #ffffff;
      border-color: var(--primary);
      transform: translateY(-2px);
    }

    .btn-primary-sm {
      background: var(--primary);
      color: white;
      border: none;
      padding: 10px 18px;
      border-radius: 10px;
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 8px rgba(0, 123, 255, 0.15);
    }
    .btn-primary-sm:hover {
      background: #0056cc;
      box-shadow: 0 4px 12px rgba(0, 123, 255, 0.25);
      transform: translateY(-2px);
    }

    /* Empty State */
    .empty-matches { padding: 2rem; text-align: center; }
    .empty-matches p { color: var(--text-muted); margin-bottom: 1rem; }

    .skeleton-list { padding: 2rem; text-align: center; color: var(--text-muted); font-size: 0.95rem; }

    .mt-4 { margin-top: 1.5rem; }
  `]
})
export class TeacherProjectDetailPage implements OnInit {
  private route = inject(ActivatedRoute);
  private teacherService = inject(TeacherService);
  private matchingService = inject(MatchingService);

  project = signal<ProjectResponse | null>(null);
  candidates = signal<MatchingResultResponse[]>([]);
  isLoadingMatches = signal(false);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      // 1. Charger le projet
      this.teacherService.getProjectById(+id).subscribe({
        next: (p) => {
          this.project.set(p);
          // 2. Charger les candidats une fois que le projet est là
          this.loadCandidates();
        },
        error: (err) => alert("Impossible de charger le projet. Vérifiez vos accès.")
      });
    }
  }

  loadCandidates() {
    const projId = this.project()?.id;
    if (!projId) return;

    this.isLoadingMatches.set(true);
    // Appel au MatchingResultController via le service centralisé
    this.matchingService.getResultsByProject(projId)
      .pipe(finalize(() => this.isLoadingMatches.set(false)))
      .subscribe({
        next: (res) => {
          // Tri chirurgical par score global décroissant
          this.candidates.set(res.sort((a, b) => b.globalScore - a.globalScore));
        },
        error: (err) => {
          console.error("Accès refusé ou erreur serveur sur le matching", err);
          this.candidates.set([]);
        }
      });
  }
}
