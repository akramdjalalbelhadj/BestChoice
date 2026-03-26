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
import { Chatbot } from '../../../../components/chatbot/chatbot';
import { FormsModule } from '@angular/forms';
import { forkJoin, map, of } from 'rxjs';

@Component({
  selector: 'app-student-item-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent, Chatbot, FormsModule],
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

  userPreferences = signal<Map<number, number>>(new Map());
  campaign = signal<CampaignResponse | null>(null);
  items = signal<any[]>([]);
  isLoading = signal(true);

  isProjectCampaign = computed(() => this.campaign()?.campaignType === MatchingCampaignType.PROJECT);
  isStableAlgorithm = computed(() => this.campaign()?.algorithmType === 'STABLE');

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : '?';
  });

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
        console.log('Campagne reçue:', data.campaign);
        console.log('Items reçus (Tableau):', data.items);
        this.campaign.set(data.campaign);
        this.items.set(Array.isArray(data.items) ? data.items : []);
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
