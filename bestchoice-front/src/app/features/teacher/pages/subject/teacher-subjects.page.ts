import { Component, inject, OnInit, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { SubjectResponse } from '../../../subject/models/subject.model';
import { finalize } from 'rxjs';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-subjects.page.html',
  styleUrl: './teacher-subjects.page.scss'
})
export class TeacherSubjectsPage implements OnInit {
  private readonly teacherService = inject(TeacherService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  isLoading = signal(true);
  searchQuery = signal('');

  subjects = this.teacherService.subjects;

  filteredSubjects = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const all = this.subjects();
    if (!query) return all;
    return all.filter(s =>
      s.title.toLowerCase().includes(query) ||
      s.keywords?.some(k => k.toLowerCase().includes(query)) ||
      s.requiredSkills?.some(sk => sk.toLowerCase().includes(query))
    );
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.teacherService.loadMySubjects(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe();
    }
  }

  toggleStatus(s: SubjectResponse) {
    this.teacherService.toggleSubjectStatus(s.id, s.active).subscribe();
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
