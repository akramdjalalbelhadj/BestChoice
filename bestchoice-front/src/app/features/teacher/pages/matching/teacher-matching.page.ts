import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatchingService } from '../../../matching/services/matching.service';
import { TeacherService } from '../../services/teacher.service';
import { MatchingResultResponse } from '../../../matching/models/matching.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-matching-results-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './teacher-matching.page.html',
  styleUrl: './teacher-matching.page.scss'
})
export class MatchingResultsViewPage implements OnInit {
  private route = inject(ActivatedRoute);
  private matchingService = inject(MatchingService);
  private teacherService = inject(TeacherService);

  campaignId = signal<number | null>(null);
  results = signal<MatchingResultResponse[]>([]);
  isLoading = signal(true);

  // On récupère les infos de la campagne depuis le service Teacher
  campaign = computed(() =>
    this.teacherService.campaigns().find(c => c.id === this.campaignId())
  );

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
}
