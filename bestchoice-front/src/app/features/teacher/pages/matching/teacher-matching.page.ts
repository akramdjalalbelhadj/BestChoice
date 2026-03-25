import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatchingService } from '../../../matching/services/matching.service';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { MatchingResultResponse } from '../../../matching/models/matching.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-matching-results-view',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './teacher-matching.page.html',
  styleUrl: './teacher-matching.page.scss'
})
export class MatchingResultsViewPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly matchingService = inject(MatchingService);
  private readonly teacherService = inject(TeacherService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  campaignId = signal<number | null>(null);
  results = signal<MatchingResultResponse[]>([]);
  isLoading = signal(true);

  campaign = computed(() =>
    this.teacherService.campaigns().find(c => c.id === this.campaignId())
  );

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
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
        next: (data) => this.results.set(data),
        error: (err) => console.error('Erreur chargement résultats', err)
      });
  }

  getScoreClass(score: number): string {
    if (score >= 0.8) return 'high';
    if (score >= 0.5) return 'medium';
    return 'low';
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
