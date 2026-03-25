import { Component, inject, OnInit, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { StudentService } from '../../services/student.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-student-campaign',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './student-campaign.page.html',
  styleUrl: './student-campaign.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StudentCampaignPage implements OnInit {
  private readonly studentService = inject(StudentService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  campaigns = this.studentService.campaigns;
  isLoading = signal(true);

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : '?';
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.studentService.loadInitialData(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe({
          next: (data) => console.log(`${data.length} campagnes chargées.`),
          error: (err) => console.error('Erreur chargement page campagnes', err)
        });
    }
  }

  logout() {
    this.studentService.clearData();
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
