import { Component, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StudentService } from '../../services/student.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import {finalize, switchMap} from 'rxjs';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-student-campaign',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './student-campaign.page.html',
  styleUrl: './student-campaign.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StudentCampaignPage implements OnInit {
  private readonly studentService = inject(StudentService);
  private readonly auth = inject(AuthStore);

  campaigns = this.studentService.campaigns;
  isLoading = signal(true);

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.studentService.loadProfile(user.userId).pipe(
        switchMap(profile => this.studentService.loadMyCampaigns(profile.id)),
        finalize(() => this.isLoading.set(false))
      ).subscribe({
        error: (err) => console.error('Erreur lors du chargement des campagnes', err)
      });
    }
  }
}
