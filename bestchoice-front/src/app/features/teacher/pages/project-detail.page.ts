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
        <a routerLink="/app/teacher/dashboard" class="back-link">← Retour au dashboard</a>
        <div class="header-main">
          <h1>{{ project()?.title || 'Chargement...' }}</h1>
          @if (project()) {
            <span class="badge-status" [class.active]="project()?.active">
              {{ project()?.active ? 'PUBLIÉ' : 'BROUILLON' }}
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
              {{ isLoadingMatches() ? 'Calcul...' : '🔄 Actualiser' }}
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
    .detail-container { padding: 2rem; max-width: 1200px; margin: 0 auto; color: #fff; }
    .detail-header { margin-bottom: 2rem; }
    .header-main { display: flex; align-items: center; gap: 15px; margin-top: 10px; }
    .back-link { color: #3b82f6; text-decoration: none; font-size: 0.9rem; }

    .detail-grid { display: grid; grid-template-columns: 1fr 1.2fr; gap: 2rem; }
    .card { background: #18181b; border: 1px solid #27272a; border-radius: 16px; padding: 2rem; }

    .stats-row { display: flex; gap: 40px; margin: 1.5rem 0; }
    label { font-size: 0.7rem; color: #71717a; text-transform: uppercase; font-weight: 700; display: block; margin-bottom: 8px; }
    .stat-val { font-size: 1.25rem; font-weight: 800; color: #3b82f6; }

    .tag-cloud { display: flex; flex-wrap: wrap; gap: 8px; }
    .tag-skill { background: rgba(59, 130, 246, 0.1); color: #3b82f6; padding: 4px 10px; border-radius: 6px; font-size: 0.8rem; border: 1px solid rgba(59,130,246,0.2); }

    .candidate-item { display: flex; align-items: center; gap: 15px; padding: 12px; background: #09090b; border: 1px solid #27272a; border-radius: 12px; margin-bottom: 10px; }
    .match-score { width: 42px; height: 42px; border: 2px solid #3b82f6; border-radius: 50%; display: grid; place-items: center; font-weight: 800; font-size: 0.75rem; color: #3b82f6; }
    .cand-info { flex: 1; }
    .cand-name { font-weight: 600; font-size: 0.9rem; }
    .cand-details { display: flex; gap: 10px; font-size: 0.7rem; color: #71717a; }

    .badge-status { font-size: 0.65rem; font-weight: 900; padding: 4px 10px; border-radius: 20px; background: #27272a; }
    .badge-status.active { background: rgba(34, 197, 94, 0.15); color: #4ade80; }
    .btn-view { background: #27272a; border: none; color: #fff; padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 0.75rem; }
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
