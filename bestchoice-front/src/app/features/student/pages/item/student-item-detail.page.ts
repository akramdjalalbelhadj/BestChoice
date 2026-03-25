import { Component, inject, OnInit, signal, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { StudentService } from '../../services/student.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { CampaignResponse } from '../../../campaign/models/campaign.model';
import { finalize } from 'rxjs';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import {MatchingCampaignType} from '../../../campaign/models/matching-campaign-type.model';

@Component({
  selector: 'app-student-item-detail', // Tu pourrais le renommer en student-campaign-items
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
  protected auth = inject(AuthStore);

  // La campagne contient déjà les listes de projets ou matières
  campaign = signal<any | null>(null);
  isLoading = signal(true);

  // Identifie si c'est une campagne de projets ou de matières
  isProjectCampaign = computed(() => this.campaign()?.campaignType === MatchingCampaignType.PROJECT);

  // Liste des items à afficher (soit projects, soit subjects)
  items = computed(() => {
    const c = this.campaign();
    if (!c) return [];
    return c.campaignType === MatchingCampaignType.PROJECT ? c.projects : c.subjects;
  });

  ngOnInit() {
    // Ici, on a juste besoin de l'ID de la campagne (passé en paramètre d'URL :id)
    const campaignId = this.route.snapshot.paramMap.get('id');
    const user = this.auth.user();

    if (!campaignId) {
      this.router.navigate(['/app/student/campaigns']);
      return;
    }

    this.isLoading.set(true);

    // On utilise une méthode du CampaignService ou du StudentService
    // qui ramène la campagne COMPLETE (avec les relations FETCH)
    this.studentService.loadInitialData(user?.userId!).subscribe(() => {
      // On récupère les détails de la campagne (assure-toi que ton service
      // renvoie bien l'objet complet avec les listes projects/subjects)
      this.studentService.loadMyCampaigns(this.studentService.studentProfile()?.id!)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe(campaigns => {
          const found = campaigns.find(c => c.id === +campaignId);
          this.campaign.set(found);
        });
    });
  }

  logout() {
    this.studentService.clearData();
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
