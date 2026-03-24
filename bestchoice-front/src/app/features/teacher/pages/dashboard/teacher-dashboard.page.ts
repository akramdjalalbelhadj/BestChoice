import { Component, inject, OnInit, computed, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-dashboard.page.html',
  styleUrl: './teacher-dashboard.page.scss'
})
export class TeacherDashboardPage implements OnInit {
  private readonly teacherService = inject(TeacherService);
  private readonly auth = inject(AuthStore);

  projects = this.teacherService.projects;
  subjects = this.teacherService.subjects;
  campaigns = this.teacherService.campaigns;

  totalCapacity = computed(() => {
    const pCap = this.projects().reduce((acc, p) => acc + (p.maxStudents || 0), 0);
    const sCap = this.subjects().reduce((acc, s) => acc + (s.maxStudents || 0), 0);
    return pCap + sCap;
  });

  activeCampaignsCount = computed(() => this.campaigns().length);

  globalOccupancyRate = computed(() => {
    const total = this.totalCapacity();
    if (total === 0) return 0;

    const assigned = this.projects().reduce((acc, p) => acc + (p.assignedStudentEmails?.length || 0), 0);
    return Math.round((assigned / total) * 100);
  });

  ngOnInit() {
    const userId = this.auth.user()?.userId;
    if (userId) {
      this.teacherService.loadAllData(userId);
    }
  }
}
