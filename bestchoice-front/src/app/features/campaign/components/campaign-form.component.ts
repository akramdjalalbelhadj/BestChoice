import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CampaignService } from '../services/campaign.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { MatchingAlgorithmType } from '../../../core/models/enums.model';
import { MatchingCampaignType } from '../models/matching-campaign-type.model';

@Component({
  selector: 'app-campaign-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="campaign-form card">
      <h2>Nouvelle Campagne de Matching</h2>

      <div class="field">
        <label>Nom de la campagne</label>
        <input type="text" formControlName="name" placeholder="Ex: Master IDL - Session 2026">
      </div>

      <div class="form-grid">
        <div class="field">
          <label>Type de matching</label>
          <select formControlName="campaignType">
            <option value="PROJECT">Projets (PFE / Tutorés)</option>
            <option value="SUBJECT">Matières / Options</option>
          </select>
        </div>
        <div class="field">
          <label>Algorithme</label>
          <select formControlName="algorithmType">
            <option value="STABLE">Stable (Gale-Shapley)</option>
            <option value="WEIGHTED">Pondéré (Global Score)</option>
            <option value="HYBRID">Hybride (Recommandé)</option>
          </select>
        </div>
      </div>

      <section class="weights-section" *ngIf="form.get('algorithmType')?.value !== 'STABLE'">
        <h3>Réglage des priorités (%)</h3>
        <div class="weight-item">
          <label>Compétences : {{ form.value.skillsWeight }}%</label>
          <input type="range" formControlName="skillsWeight" min="0" max="100">
        </div>
        <div class="weight-item">
          <label>Intérêts : {{ form.value.interestsWeight }}%</label>
          <input type="range" formControlName="interestsWeight" min="0" max="100">
        </div>
        <div class="weight-info">
          Le reste ({{ remainingWeight() }}%) sera alloué au type de travail.
        </div>
      </section>

      <div class="actions">
        <button type="submit" [disabled]="form.invalid">Créer la campagne</button>
      </div>
    </form>
  `
})
export class CampaignFormComponent {
  private fb = inject(FormBuilder);
  private campaignService = inject(CampaignService);
  private auth = inject(AuthStore);

  form = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    academicYear: ['2025-2026'],
    semester: [1],
    campaignType: [MatchingCampaignType.PROJECT],
    algorithmType: [MatchingAlgorithmType.HYBRID],
    skillsWeight: [33],
    interestsWeight: [33]
  });

  remainingWeight() {
    return 100 - (this.form.value.skillsWeight || 0) - (this.form.value.interestsWeight || 0);
  }

  onSubmit() {
    const teacherId = this.auth.user()?.userId;
    if (!teacherId) return;

    const payload = {
      ...this.form.getRawValue(),
      teacherId,
      skillsWeight: (this.form.value.skillsWeight || 0) / 100,
      interestsWeight: (this.form.value.interestsWeight || 0) / 100,
      workTypeWeight: this.remainingWeight() / 100
    };

    this.campaignService.create(payload as any).subscribe();
  }
}
