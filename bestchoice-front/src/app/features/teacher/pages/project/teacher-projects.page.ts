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

  isLoading    = signal(true);
  searchQuery  = signal('');
  currentPage  = signal(1);
  readonly pageSize = 5;

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

  totalPages = computed(() => Math.max(1, Math.ceil(this.filteredProjects().length / this.pageSize)));

  paginatedProjects = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredProjects().slice(start, start + this.pageSize);
  });

  pageNumbers = computed(() => {
    const total   = this.totalPages();
    const current = this.currentPage();
    const maxVisible = 5;
    let start = Math.max(1, current - Math.floor(maxVisible / 2));
    let end   = Math.min(total, start + maxVisible - 1);
    if (end - start + 1 < maxVisible) start = Math.max(1, end - maxVisible + 1);
    const pages: number[] = [];
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  });

  paginationInfo = computed(() => {
    const total = this.filteredProjects().length;
    if (total === 0) return '';
    const start = (this.currentPage() - 1) * this.pageSize + 1;
    const end   = Math.min(this.currentPage() * this.pageSize, total);
    return `${start}–${end} sur ${total} résultat${total > 1 ? 's' : ''}`;
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

  onSearch(query: string) {
    this.searchQuery.set(query);
    this.currentPage.set(1);
  }

  prevPage()          { this.currentPage.update(p => Math.max(1, p - 1)); }
  nextPage()          { this.currentPage.update(p => Math.min(this.totalPages(), p + 1)); }
  goToPage(n: number) { this.currentPage.set(n); }

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
