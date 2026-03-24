import {CampaignService} from '../services/campaign.service';
import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-campaign-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="campaign-grid">
      @for (c of campaigns(); track c.id) {
        <div class="campaign-card">
          <div class="card-header">
            <h4>{{ c.name }}</h4>
            <span class="badge">{{ c.campaignType }}</span>
          </div>
          <div class="card-body">
            <p>{{ c.studentsCount }} étudiants inscrits</p>
            <p>{{ c.itemsCount }} {{ c.campaignType === 'PROJECT' ? 'projets' : 'matières' }}</p>
          </div>
          <div class="card-footer">
            <button class="btn-danger" (click)="delete(c.id)">Supprimer</button>
            <button class="btn-primary" [routerLink]="['/campaign', c.id]">Gérer</button>
          </div>
        </div>
      }
    </div>
  `
})
export class CampaignListComponent {
  private campaignService = inject(CampaignService);
  campaigns = this.campaignService.campaigns;

  delete(id: number) {
    if (confirm('Supprimer cette campagne ?')) {
      this.campaignService.delete(id).subscribe();
    }
  }
}
