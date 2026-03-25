import { Component, inject, OnInit, signal, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { finalize } from 'rxjs';
import { TeacherService } from '../../services/teacher.service';
import { MatchingService } from '../../../matching/services/matching.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { ProjectResponse } from '../../../project/models/project.model';
import { MatchingResultResponse } from '../../../matching/models/matching.model';

@Component({
  selector: 'app-teacher-project-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-project-detail.page.html',
  styleUrl: './teacher-project-detail.page.scss'
})
export class TeacherProjectDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly teacherService = inject(TeacherService);
  private readonly matchingService = inject(MatchingService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  project = signal<ProjectResponse | null>(null);
  candidates = signal<MatchingResultResponse[]>([]);
  isLoading = signal(false);
  isLoadingMatches = signal(false);

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.loadProjectData(+projectId);
    }
  }

  private loadProjectData(id: number): void {
    this.isLoading.set(true);
    this.teacherService.getProjectById(id)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (data) => { this.project.set(data); this.loadCandidates(); },
        error: (err) => console.error('Erreur chargement projet', err)
      });
  }

  loadCandidates() {
    const currentProject = this.project();
    if (!currentProject) return;
    this.isLoadingMatches.set(true);
    this.matchingService.getResultsByProject(1, currentProject.id)
      .pipe(finalize(() => this.isLoadingMatches.set(false)))
      .subscribe({
        next: (results: MatchingResultResponse[]) => {
          this.candidates.set(results.sort((a, b) => b.globalScore - a.globalScore));
        },
        error: (err: any) => console.error('Erreur matching', err)
      });
  }

  getScoreClass(score: number): string {
    if (score >= 0.75) return 'score-high';
    if (score >= 0.45) return 'score-medium';
    return 'score-low';
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
