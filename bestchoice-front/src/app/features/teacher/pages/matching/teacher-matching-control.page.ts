import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { MatchingService } from '../../../matching/services/matching.service';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-matching-control',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './teacher-matching-control.page.html',
  styleUrl: './teacher-matching-control.page.scss'
})
export class TeacherMatchingControlPage implements OnInit {
  private readonly matchingService = inject(MatchingService);
  private readonly teacherService = inject(TeacherService);
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  // État des données
  campaigns = this.teacherService.campaigns;
  selectedCampaignId = signal<number | null>(null);
  isProcessing = signal(false);

  // Poids (0.33 = 33%)
  weights = signal({ skills: 0.33, interests: 0.33 });

  // Calcul du 3ème poids (WorkType)
  remainingWeight = computed(() => {
    const total = 1 - (this.weights().skills + this.weights().interests);
    return Math.max(0, parseFloat(total.toFixed(2)));
  });

  // Campagne actuellement sélectionnée
  selectedCampaign = computed(() =>
    this.campaigns().find(c => c.id === this.selectedCampaignId())
  );

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.teacherService.loadAllData(user.userId);
    }
  }

  onCampaignChange(id: string | number) {
    const cid = Number(id);
    this.selectedCampaignId.set(cid);

    const campaign = this.campaigns().find(c => c.id === cid);
    if (campaign) {
      // On initialise les sliders avec les valeurs par défaut de la campagne
      this.weights.set({
        skills: campaign.skillsWeight,
        interests: campaign.interestsWeight
      });
    }
  }

  updateWeight(key: 'skills' | 'interests', event: Event) {
    const valPercent = +(event.target as HTMLInputElement).value;
    const valDecimal = Math.round(valPercent) / 100;

    this.weights.update(current => {
      const next = { ...current, [key]: valDecimal };

      if (next.skills + next.interests > 1) {
        if (key === 'skills') next.interests = Math.round((1 - valDecimal) * 100) / 100;
        else next.skills = Math.round((1 - valDecimal) * 100) / 100;
      }

      return next;
    });
  }

  runMatching() {
    const campaign = this.selectedCampaign();
    if (!campaign) return;

    this.isProcessing.set(true);

    this.teacherService.executeMatching(campaign.id)
      .pipe(finalize(() => this.isProcessing.set(false)))
      .subscribe({
        next: (response) => {
          this.router.navigate(['/app/teacher/campaigns/results', campaign.id]);
        },
        error: (err) => {
          console.error('Erreur matching:', err);
          alert('Le calcul a échoué. Vérifiez que la campagne contient assez de vœux étudiants.');
        }
      });
  }
}
