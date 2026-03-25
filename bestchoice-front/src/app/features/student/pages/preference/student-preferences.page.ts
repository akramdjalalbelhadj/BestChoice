import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { StudentService } from '../../services/student.service';
import { CampaignService } from '../../../campaign/services/campaign.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { finalize, forkJoin } from 'rxjs';

@Component({
  selector: 'app-student-preferences',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, DragDropModule, ThemeToggleComponent],
  templateUrl: './student-preferences.page.html',
  styleUrl: './student-preferences.page.scss'
})
export class StudentPreferencesPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly studentService = inject(StudentService);
  private readonly campaignService = inject(CampaignService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  // ── Mode d'affichage ──────────────────────────────────────────────────────
  mode: 'summary' | 'ranking' = 'summary';

  // ── Mode résumé ───────────────────────────────────────────────────────────
  groupedPreferences = signal<any[]>([]);

  // ── Mode classement ───────────────────────────────────────────────────────
  itemsToRank = signal<any[]>([]);
  campaignId: number | null = null;
  campaignType: string | null = null;
  isSubmitting = signal(false);
  isSuccess = signal(false);

  // ── Partagé ───────────────────────────────────────────────────────────────
  isLoading = signal(true);

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : '?';
  });

  ngOnInit() {
    const campaignIdParam = this.route.snapshot.queryParamMap.get('campaignId');
    this.campaignId = campaignIdParam ? Number(campaignIdParam) : null;

    if (this.campaignId) {
      this.mode = 'ranking';
      this.loadCampaignData();
    } else {
      this.mode = 'summary';
      this.loadMyPreferences();
    }
  }

  // ── Mode résumé ───────────────────────────────────────────────────────────

  private loadMyPreferences() {
    const studentId = this.studentService.studentProfile()?.id;
    if (!studentId) { this.isLoading.set(false); return; }

    this.studentService.getPreferences(studentId).pipe(
      finalize(() => this.isLoading.set(false))
    ).subscribe(prefs => {
      if (!prefs.length) { this.groupedPreferences.set([]); return; }

      const uniqueCampaignIds = [...new Set(prefs.map(p => p.campaignId))];

      forkJoin(
        uniqueCampaignIds.map(cid => this.campaignService.getCompleteCampaign(cid))
      ).subscribe(campaignDataList => {
        const groups = uniqueCampaignIds.map((cid, idx) => {
          const { campaign, items } = campaignDataList[idx];
          const campaignPrefs = prefs
            .filter(p => p.campaignId === cid)
            .sort((a, b) => a.rank - b.rank);

          return {
            campaign,
            preferences: campaignPrefs.map(p => {
              const itemId = campaign.campaignType === 'PROJECT' ? p.projectId : p.subjectId;
              const item = items.find((i: any) => i.id === itemId);
              return {
                ...p,
                itemTitle: item?.title ?? `Élément #${itemId}`,
                itemTeacher: item?.teacherName ?? ''
              };
            })
          };
        });
        this.groupedPreferences.set(groups);
      });
    });
  }

  goToRank(campaignId: number) {
    this.router.navigate(['/app/student/preferences'], { queryParams: { campaignId } });
  }

  // ── Mode classement ───────────────────────────────────────────────────────

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
    this.itemsToRank.set(list.map((item, index) => ({ ...item, tempRank: index + 1 })));
  }

  submitAllPreferences() {
    const studentId = this.studentService.studentProfile()?.id;
    this.isSubmitting.set(true);

    const requests = this.itemsToRank().map(item => ({
      studentId,
      campaignId: this.campaignId,
      projectId: this.campaignType === 'PROJECT' ? item.id : null,
      subjectId: this.campaignType === 'SUBJECT' ? item.id : null,
      rank: item.tempRank
    }));

    forkJoin(requests.map(req => this.studentService.submitPreference(req)))
      .subscribe({
        next: () => {
          this.isSuccess.set(true);
          setTimeout(() => this.router.navigate(['/app/student/dashboard']), 2000);
        },
        error: (err) => alert(err?.error?.message ?? 'Une erreur est survenue.')
      });
  }

  logout() {
    this.studentService.clearData();
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
