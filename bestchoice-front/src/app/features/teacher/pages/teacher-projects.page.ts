import { Component, inject, OnInit, ChangeDetectionStrategy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TeacherService } from '../services/teacher.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { ProjectResponse } from '../../project/models/project.model';
import { finalize } from 'rxjs';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page-container">
      <header class="page-header">
        <div class="title-group">
          <h1>Mes Propositions</h1>
          <p class="text-muted">Gérez vos offres et suivez l'affectation des étudiants en temps réel.</p>
        </div>
        <button class="btn-primary" routerLink="./create">
          <span class="icon">＋</span> Publier un projet
        </button>
      </header>

      <section class="filter-card">
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input
            type="text"
            [(ngModel)]="searchQuery"
            placeholder="Rechercher un projet par titre ou mots-clés..."
          />
        </div>
      </section>

      <section class="table-card">
        <div class="table-responsive">
          <table class="pro-table">
            <thead>
              <tr>
                <th>Projet & Thématiques</th>
                <th>Type / ECTS</th>
                <th>Compétences clés</th>
                <th>Occupation</th>
                <th>Statut</th>
                <th class="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              @if (isLoading()) {
                @for (i of [1,2,3,4]; track i) {
                  <tr><td colspan="6"><div class="skeleton table-row-skel"></div></td></tr>
                }
              } @else {
                @for (p of filteredProjects(); track p.id) {
                  <tr class="project-row">
                    <td>
                      <div class="title-cell">
                        <span class="main-title">{{ p.title }}</span>
                        <div class="keyword-list">
                          @for (k of p.keywords; track k) {
                            <span class="keyword-tag">#{{ k }}</span>
                          }
                        </div>
                      </div>
                    </td>
                    <td>
                      <span class="type-badge">{{ p.workType }}</span>
                      <div class="text-xs text-muted">S1 • 6 ECTS</div>
                    </td>
                    <td>
                      <div class="skills-stack">
                        @for (s of p.requiredSkills; track s) {
                          <span class="skill-mini">{{ s }}</span>
                        }
                      </div>
                    </td>
                    <td>
                      <div class="capacity-visual">
                        <span class="cap-text"><strong>{{ p.assignedStudentEmails.length }}</strong> / {{ p.maxStudents }}</span>
                        <div class="mini-progress">
                          <div class="fill" [style.width.%]="(p.assignedStudentEmails.length/p.maxStudents)*100"></div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <button
                        class="status-toggle"
                        [class.active]="p.active"
                        (click)="toggleStatus(p)"
                        [title]="p.active ? 'Désactiver l\\'offre' : 'Publier l\\'offre'">
                        {{ p.active ? 'Public' : 'Brouillon' }}
                      </button>
                    </td>
                    <td class="text-right">
                      <div class="action-menu">
                        <button class="action-btn" [routerLink]="['./edit', p.id]" title="Modifier">✏️</button>
                        <button
                          class="action-btn highlight"
                          [routerLink]="['/app/teacher/projects', p.id]"
                          title="Voir les candidats et détails">🧬</button>
                        <button class="action-btn danger" (click)="deactivate(p.id)" title="Retirer l'offre">🚫</button>
                      </div>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="6">
                      <div class="empty-table">
                        <p>Aucun projet trouvé.</p>
                      </div>
                    </td>
                  </tr>
                }
              }
            </tbody>
          </table>
        </div>
      </section>
    </div>
  `,
  styleUrl: './teacher-projects.page.scss'
})
export class TeacherProjectsPage implements OnInit {
  private teacherService = inject(TeacherService);
  private auth = inject(AuthStore);

  isLoading = signal(true);
  searchQuery = signal('');

  // Utilisation directe du signal du service
  projects = this.teacherService.projects;

  // Filtrage chirurgical côté client
  filteredProjects = computed(() => {
    const query = this.searchQuery().toLowerCase();
    return this.projects().filter(p =>
      p.title.toLowerCase().includes(query) ||
      p.keywords.some(k => k.toLowerCase().includes(query))
    );
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.teacherService.loadMyProjects(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe();
    } else {
      this.isLoading.set(false);
    }
  }

  toggleStatus(project: ProjectResponse) {
    if (project.active) {
      this.teacherService.deactivate(project.id).subscribe();
    } else {
      this.teacherService.activate(project.id).subscribe();
    }
  }

  deactivate(id: number) {
    if (confirm('Voulez-vous vraiment retirer ce projet ? Il ne sera plus visible par les étudiants.')) {
      this.teacherService.deactivate(id).subscribe();
    }
  }
}
