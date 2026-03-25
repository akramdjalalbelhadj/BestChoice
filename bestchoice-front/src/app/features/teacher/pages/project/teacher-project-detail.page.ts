import { Component, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { TeacherService } from '../../services/teacher.service';
import { MatchingService } from '../../../matching/services/matching.service';
import { ProjectResponse } from '../../../project/models/project.model';
import { MatchingResultResponse } from '../../../matching/models/matching.model';

@Component({
  selector: 'app-teacher-project-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-project-detail.page.html',
  styleUrl: './teacher-project-detail.page.scss'
})
export class TeacherProjectDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly teacherService = inject(TeacherService);
  private readonly matchingService = inject(MatchingService);

  project = signal<ProjectResponse | null>(null);
  candidates = signal<MatchingResultResponse[]>([]);
  isLoading = signal(false);
  isLoadingMatches = signal(false);

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.loadProjectData(+projectId);
    }
  }

  /**
   * Charge les données du projet et initialise le chargement des candidats
   */
  private loadProjectData(id: number): void {
    this.isLoading.set(true);
    this.teacherService.getProjectById(id)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (data) => {
          this.project.set(data);
          this.loadCandidates();
        },
        error: (err) => console.error('Erreur chargement projet', err)
      });
  }

  /**
   * Récupère les recommandations de l'algorithme pour ce projet
   */
  loadCandidates() {
    const currentProject = this.project();
    if (!currentProject) return;

    this.isLoadingMatches.set(true);

    const campaignId = 1;

    this.matchingService.getResultsByProject(campaignId, currentProject.id)
      .pipe(finalize(() => this.isLoadingMatches.set(false)))
      .subscribe({
        next: (results: MatchingResultResponse[]) => {
          this.candidates.set(results.sort((a, b) => b.globalScore - a.globalScore));
        },
        error: (err: any) => console.error('Erreur matching', err)
      });
  }

  /**
   * Helper pour le style dynamique des scores
   */
  getScoreClass(score: number): string {
    if (score >= 0.75) return 'score-high';
    if (score >= 0.45) return 'score-medium';
    return 'score-low';
  }
}
