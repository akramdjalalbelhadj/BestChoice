import { Component, inject, OnInit, signal, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { finalize } from 'rxjs';
import { SubjectService } from '../../../subject/services/subject.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { SubjectResponse } from '../../../subject/models/subject.model';

@Component({
  selector: 'app-teacher-subject-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './teacher-subject-detail.page.html',
  styleUrl: './teacher-subject-detail.page.scss'
})
export class TeacherSubjectDetailPage implements OnInit {
  private readonly route          = inject(ActivatedRoute);
  private readonly subjectService = inject(SubjectService);
  protected readonly auth         = inject(AuthStore);
  private readonly router         = inject(Router);

  subject   = signal<SubjectResponse | null>(null);
  isLoading = signal(false);

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isLoading.set(true);
      this.subjectService.getById(+id)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe({
          next: data => this.subject.set(data),
          error: err  => console.error('Erreur chargement matière', err)
        });
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
