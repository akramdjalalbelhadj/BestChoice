import { Component, inject, OnInit, computed, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-dashboard.page.html',
  styleUrl: './teacher-dashboard.page.scss'
})
export class TeacherDashboardPage implements OnInit {
  private readonly teacherService = inject(TeacherService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  projects = this.teacherService.projects;
  subjects = this.teacherService.subjects;
  campaigns = this.teacherService.campaigns;

  projectCapacity = computed(() =>
    this.projects().reduce((acc, p) => acc + (p.maxStudents || 0), 0)
  );

  subjectCapacity = computed(() =>
    this.subjects().reduce((acc, s) => acc + (s.maxStudents || 0), 0)
  );

  totalCapacity = computed(() => this.projectCapacity() + this.subjectCapacity());

  activeCampaignsCount = computed(() => this.campaigns().length);

  globalOccupancyRate = computed(() => {
    const total = this.totalCapacity();
    if (total === 0) return 0;
    const assigned = this.projects().reduce((acc, p) => acc + (p.assignedStudentEmails?.length || 0), 0);
    return Math.round((assigned / total) * 100);
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    const userId = this.auth.user()?.userId;
    if (userId) {
      this.teacherService.loadAllData(userId);
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
