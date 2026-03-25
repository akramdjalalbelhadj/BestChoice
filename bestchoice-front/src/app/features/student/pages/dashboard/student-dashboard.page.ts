import { Component, inject, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStore } from '../../../../core/auth/auth.store';
import { StudentService } from '../../services/student.service';
import { finalize } from 'rxjs';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './student-dashboard.page.html',
  styleUrl: './student-dashboard.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StudentDashboardPage implements OnInit {
  protected readonly auth = inject(AuthStore);
  private readonly studentService = inject(StudentService);
  private readonly router = inject(Router);

  profile = this.studentService.studentProfile;
  topMatches = this.studentService.topMatches;

  isLoading = signal(true);
  preferenceCount = signal(0);

  completion = computed(() => {
    const p = this.profile();
    if (!p) return 0;
    let score = 0;
    if (p.studyYear) score += 25;
    if (p.skill && p.skill.length > 0) score += 25;
    if (p.preferredWorkTypes && p.preferredWorkTypes.length > 0) score += 25;
    // Ajout d'une condition pour atteindre 100% ou rester à 75% selon tes besoins
    if (score === 75) score = 100;
    return score;
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.studentService.loadProfile(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe(student => {
          if (student?.id) {
            this.studentService.loadTopMatches(student.id).subscribe();
            this.studentService.getPreferences(student.id).subscribe(prefs => {
              this.preferenceCount.set(prefs.length);
            });
          }
        });
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
