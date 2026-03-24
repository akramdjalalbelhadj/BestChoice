import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatchingService } from '../../../matching/services/matching.service';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-matching-control',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './matching-control.page.html',
  styleUrl: './matching-control.page.scss'
})
export class MatchingControlPage implements OnInit {
  private readonly matchingService = inject(MatchingService);
  private readonly teacherService = inject(TeacherService);
  private readonly auth = inject(AuthStore);

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
    const valDecimal = parseFloat((valPercent / 100).toFixed(2));

    this.weights.update(current => {
      const next = { ...current, [key]: valDecimal };

      // Si la somme dépasse 1.0 (100%), on ajuste l'autre curseur
      if (next.skills + next.interests > 1) {
        if (key === 'skills') next.interests = parseFloat((1 - valDecimal).toFixed(2));
        else next.skills = parseFloat((1 - valDecimal).toFixed(2));
      }

      return next;
    });
  }

  runMatching() {
    const campaign = this.selectedCampaign();
    if (!campaign) return;

    this.isProcessing.set(true);

    this.matchingService.runMatching(campaign.id)
      .pipe(finalize(() => this.isProcessing.set(false)))
      .subscribe({
        next: (res) => {
          alert(`Calcul terminé !\n${res.studentsProcessed} étudiants traités.`);
        },
        error: (err) => {
          alert('Erreur lors du calcul. Vérifiez que la campagne contient des participants.');
        }
      });
  }
}
