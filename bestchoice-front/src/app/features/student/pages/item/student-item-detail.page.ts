import { Component, inject, OnInit, signal, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { StudentService } from '../../services/student.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { CampaignResponse } from '../../../campaign/models/campaign.model';
import { CampaignService } from '../../../campaign/services/campaign.service';
import { finalize, switchMap } from 'rxjs';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { MatchingCampaignType } from '../../../campaign/models/matching-campaign-type.model';

@Component({
  selector: 'app-student-item-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './student-item-detail.page.html',
  styleUrl: './student-item-detail.page.scss'
})
export class StudentItemDetailPage implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private studentService = inject(StudentService);
  private campaignService = inject(CampaignService);
  protected auth = inject(AuthStore);

  // État local via Signals
  campaign = signal<CampaignResponse | null>(null);
  items = signal<any[]>([]);
  isLoading = signal(true);

  // Helper pour savoir quel type de data on manipule
  isProjectCampaign = computed(() => this.campaign()?.campaignType === MatchingCampaignType.PROJECT);

  ngOnInit() {
    const campaignId = this.route.snapshot.paramMap.get('id');
    const user = this.auth.user();

    if (!campaignId || !user) {
      this.router.navigate(['/app/student/campaigns']);
      return;
    }

    this.isLoading.set(true);

    /**
     * LOGIQUE :
     * 1. Charger le profil étudiant (nécessaire pour le contexte global)
     * 2. Charger la campagne spécifique et ses items associés (projets ou matières)
     */
    this.studentService.loadInitialData(user.userId).pipe(
      switchMap(() => this.campaignService.getCompleteCampaign(+campaignId)),
      finalize(() => this.isLoading.set(false))
    ).subscribe({
      next: (data) => {
        this.campaign.set(data.campaign);
        this.items.set(data.items);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des données de campagne', err);
        this.router.navigate(['/app/student/campaigns']);
      }
    });
  }

  logout() {
    this.studentService.clearData();
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
