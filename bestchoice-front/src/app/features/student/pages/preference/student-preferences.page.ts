import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { StudentService } from '../../services/student.service';
import { CampaignService } from '../../../campaign/services/campaign.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import {finalize, forkJoin, delay, tap, switchMap, of} from 'rxjs';

@Component({
  selector: 'app-student-preferences',
  standalone: true,
  imports: [CommonModule, RouterLink, DragDropModule],
  templateUrl: './student-preferences.page.html',
  styleUrl: './student-preferences.page.scss'
})
export class StudentPreferencesPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly studentService = inject(StudentService);
  private readonly campaignService = inject(CampaignService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  itemsToRank = signal<any[]>([]);
  isLoading = signal(true);
  isSubmitting = signal(false);
  isSuccess = signal(false);
  campaignId: number | null = null;
  campaignType: string | null = null;

  ngOnInit() {
    this.campaignId = Number(this.route.snapshot.queryParamMap.get('campaignId'));

    if (!this.campaignId) {
      this.router.navigate(['/app/student/campaigns']);
      return;
    }

    this.loadCampaignData();
  }

  private loadCampaignData() {
    this.isLoading.set(true);
    this.campaignService.getCompleteCampaign(this.campaignId!).pipe(
      finalize(() => this.isLoading.set(false))
    ).subscribe(data => {
      this.campaignType = data.campaign.campaignType;
      this.itemsToRank.set(data.items.map((item, index) => ({
        ...item,
        tempRank: index + 1
      })));
    });
  }

  drop(event: CdkDragDrop<string[]>) {
    const list = [...this.itemsToRank()];
    moveItemInArray(list, event.previousIndex, event.currentIndex);

    this.itemsToRank.set(list.map((item, index) => ({
      ...item,
      tempRank: index + 1
    })));
  }

  /**
   * ACTION ONE-SHOT : On envoie tout au backend
   */
  submitAllPreferences() {
    const studentId = this.studentService.studentProfile()?.id;
    this.isSubmitting.set(true);

    const requests = this.itemsToRank().map(item => ({
      studentId,
      campaignId: this.campaignId,
      projectId: this.campaignType === 'PROJECT' ? item.id : null,
      rank: item.tempRank
    }));

    forkJoin(requests.map(req => this.studentService.submitPreference(req)))
      .subscribe({
        next: () => {
          this.isSuccess.set(true);
          setTimeout(() => this.router.navigate(['/app/student/dashboard']), 2000);
        },
        error: (err) => alert(err.error.message)
      });
  }
}
