import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatchingService } from '../../../matching/services/matching.service';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-matching-control',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './teacher-matching-control.page.html',
  styleUrl: './teacher-matching-control.page.scss'
})
export class TeacherMatchingControlPage implements OnInit {
  private readonly matchingService = inject(MatchingService);
  private readonly teacherService = inject(TeacherService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  campaigns = this.teacherService.campaigns;
  selectedCampaignId = signal<number | null>(null);
  isProcessing = signal(false);

  weights = signal({ skills: 0.33, interests: 0.33 });

  remainingWeight = computed(() => {
    const total = 1 - (this.weights().skills + this.weights().interests);
    return Math.max(0, parseFloat(total.toFixed(2)));
  });

  selectedCampaign = computed(() =>
    this.campaigns().find(c => c.id === this.selectedCampaignId())
  );

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

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
      this.weights.set({ skills: campaign.skillsWeight, interests: campaign.interestsWeight });
    }
  }

  updateWeight(key: 'skills' | 'interests', event: Event) {
    const valDecimal = Math.round(+(event.target as HTMLInputElement).value) / 100;
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
        next: () => this.router.navigate(['/app/teacher/campaigns/results', campaign.id]),
        error: (err) => {
          console.error('Erreur matching:', err);
          alert('Le calcul a échoué. Vérifiez que la campagne contient assez de vœux étudiants.');
        }
      });
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
