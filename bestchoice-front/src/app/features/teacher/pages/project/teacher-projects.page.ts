import { Component, inject, OnInit, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { ProjectResponse } from '../../../project/models/project.model';
import { finalize } from 'rxjs';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-projects.page.html',
  styleUrl: './teacher-projects.page.scss'
})
export class TeacherProjectsPage implements OnInit {
  private readonly teacherService = inject(TeacherService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  isLoading = signal(true);
  searchQuery = signal('');

  projects = this.teacherService.projects;

  filteredProjects = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const allProjects = this.projects();
    if (!query) return allProjects;
    return allProjects.filter(p =>
      p.title.toLowerCase().includes(query) ||
      p.keywords?.some(k => k.toLowerCase().includes(query)) ||
      p.requiredSkills?.some(s => s.toLowerCase().includes(query))
    );
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.teacherService.loadMyProjects(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe();
    }
  }

  toggleStatus(p: ProjectResponse) {
    const msg = p.active
      ? 'Voulez-vous vraiment passer ce projet en Brouillon ?'
      : 'Voulez-vous rendre ce projet Public ?';
    if (confirm(msg)) {
      this.teacherService.toggleProjectStatus(p.id, p.active).subscribe();
    }
  }

  deleteProject(id: number) {
    if (confirm('Voulez-vous vraiment retirer ce projet ?')) {
      this.teacherService.toggleProjectStatus(id, true).subscribe();
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
