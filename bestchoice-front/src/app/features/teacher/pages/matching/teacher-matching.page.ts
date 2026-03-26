import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { finalize } from 'rxjs';
import { MatchingService } from '../../../matching/services/matching.service';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { MatchingResultResponse } from '../../../matching/models/matching.model';

interface ProjectGroup {
  projectId: number | null;
  projectName: string;
  results: MatchingResultResponse[];
  avgScore: number;
}

@Component({
  selector: 'app-matching-results-view',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './teacher-matching.page.html',
  styleUrl: './teacher-matching.page.scss'
})
export class MatchingResultsViewPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly matchingService = inject(MatchingService);
  private readonly teacherService = inject(TeacherService);
  protected readonly auth = inject(AuthStore);

  campaignId = signal<number | null>(null);
  results = signal<MatchingResultResponse[]>([]);
  isLoading = signal(true);

  campaign = computed(() => this.teacherService.campaigns().find(c => c.id === this.campaignId()));

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : '?';
  });

  groupedResults = computed(() => {
    const data = this.results();
    const groups: Record<string, ProjectGroup> = {};

    data.forEach(r => {
      const key = r.projectId?.toString() ?? r.subjectId?.toString() ?? 'unknown';
      if (!groups[key]) {
        groups[key] = {
          projectId: r.projectId,
          projectName: r.projectName || r.subjectName || `Projet #${r.projectId ?? r.subjectId}`,
          results: [],
          avgScore: 0
        };
      }
      groups[key].results.push(r);
    });

    return Object.values(groups).map(group => {
      const total = group.results.reduce((acc, curr) => acc + +curr.globalScore, 0);
      group.avgScore = (total / group.results.length) * 100;
      group.results.sort((a, b) => (a.recommendationRank ?? 99) - (b.recommendationRank ?? 99));
      return group;
    }).sort((a, b) => b.avgScore - a.avgScore);
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.campaignId.set(+id);
      this.loadResults(+id);
    }
  }

  loadResults(id: number) {
    this.isLoading.set(true);
    this.matchingService.getResultsByCampaign(id)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (data) => {
          const enriched = data.map(result => {
            const student = this.teacherService.allStudents().find(s => s.id === result.studentId);
            return {
              ...result,
              studentName: student
                ? `${student.firstName} ${student.lastName}`
                : `Étudiant #${result.studentId}`
            };
          });
          this.results.set(enriched);
        }
      });
  }

  getScoreClass(score: number): string {
    if (score >= 0.7) return 'high';
    if (score >= 0.4) return 'medium';
    return 'low';
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
