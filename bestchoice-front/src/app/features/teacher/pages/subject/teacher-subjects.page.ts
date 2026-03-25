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

  isLoading   = signal(true);
  searchQuery = signal('');
  currentPage = signal(1);
  readonly pageSize = 5;

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

  totalPages = computed(() => Math.max(1, Math.ceil(this.filteredSubjects().length / this.pageSize)));

  paginatedSubjects = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredSubjects().slice(start, start + this.pageSize);
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
    const total = this.filteredSubjects().length;
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
      this.teacherService.loadMySubjects(user.userId)
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

  toggleStatus(s: SubjectResponse) {
    const msg = s.active
      ? 'Voulez-vous vraiment passer cette matière en Brouillon ?'
      : 'Voulez-vous rendre cette matière Publique ?';
    if (confirm(msg)) {
      this.teacherService.toggleSubjectStatus(s.id, s.active).subscribe();
    }
  }

  deleteSubject(id: number) {
    if (confirm('Voulez-vous vraiment supprimer cette matière ?')) {
      this.teacherService.toggleSubjectStatus(id, true).subscribe();
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
