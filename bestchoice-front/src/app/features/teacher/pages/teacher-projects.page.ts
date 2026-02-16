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
  styles: [`
    :host { --bg: #09090b; --card: #18181b; --border: #27272a; --primary: #3b82f6; --text-muted: #71717a; display: block; background: var(--bg); color: #fff; min-height: 100vh; }
    .page-container { padding: 2.5rem; max-width: 1300px; margin: 0 auto; }

    /* Header */
    .page-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2.5rem; }
    h1 { font-size: 1.8rem; font-weight: 800; margin: 0; letter-spacing: -0.02em; }

    /* Filters */
    .filter-card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 1rem; margin-bottom: 1.5rem; }
    .search-box { position: relative; display: flex; align-items: center; }
    .search-icon { position: absolute; left: 1rem; opacity: 0.5; }
    .search-box input { width: 100%; background: #09090b; border: 1px solid var(--border); border-radius: 8px; padding: 0.75rem 1rem 0.75rem 2.8rem; color: #fff; outline: none; transition: border-color 0.2s; }
    .search-box input:focus { border-color: var(--primary); }

    /* Table */
    .table-card { background: var(--card); border: 1px solid var(--border); border-radius: 16px; overflow: hidden; }
    .pro-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
    .pro-table th { background: #1c1c1f; padding: 1rem; text-align: left; font-size: 0.75rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700; letter-spacing: 0.05em; }
    .project-row { border-bottom: 1px solid var(--border); transition: background 0.2s; }
    .project-row:hover { background: rgba(255,255,255,0.02); }
    .project-row td { padding: 1.25rem 1rem; }

    /* Project Info Cells */
    .title-cell { display: flex; flex-direction: column; gap: 6px; }
    .main-title { font-weight: 700; color: #fff; font-size: 0.95rem; }
    .keyword-list { display: flex; gap: 8px; }
    .keyword-tag { font-size: 0.75rem; color: var(--primary); opacity: 0.8; }

    .type-badge { font-size: 0.7rem; font-weight: 700; background: #27272a; padding: 3px 8px; border-radius: 6px; }
    .skills-stack { display: flex; flex-wrap: wrap; gap: 4px; max-width: 200px; }
    .skill-mini { font-size: 0.65rem; background: rgba(59, 130, 246, 0.1); color: var(--primary); padding: 2px 6px; border-radius: 4px; border: 1px solid rgba(59, 130, 246, 0.2); }

    /* Occupation UX */
    .capacity-visual { width: 120px; }
    .cap-text { font-size: 0.85rem; display: block; margin-bottom: 4px; }
    .mini-progress { height: 4px; background: #27272a; border-radius: 2px; overflow: hidden; }
    .fill { height: 100%; background: #22c55e; transition: width 0.6s ease; }

    /* Status & Actions */
    .status-toggle { background: #27272a; border: 1px solid var(--border); color: var(--text-muted); padding: 5px 12px; border-radius: 20px; font-size: 0.75rem; font-weight: 700; cursor: pointer; transition: all 0.2s; }
    .status-toggle.active { background: rgba(34, 197, 94, 0.1); color: #4ade80; border-color: rgba(34, 197, 94, 0.3); }

    .action-menu { display: flex; gap: 6px; justify-content: flex-end; }
    .action-btn { background: #27272a; border: 1px solid var(--border); color: #fff; padding: 8px; border-radius: 8px; cursor: pointer; transition: 0.2s; }
    .action-btn:hover { background: #3f3f46; }
    .action-btn.danger:hover { background: rgba(239, 68, 68, 0.1); color: #ef4444; border-color: #ef4444; }
    .action-btn.highlight:hover { background: rgba(59, 130, 246, 0.1); color: var(--primary); border-color: var(--primary); }

    /* Skeletons */
    .skeleton { background: linear-gradient(90deg, #1c1c1f 25%, #27272a 50%, #1c1c1f 75%); background-size: 200% 100%; animation: loading 1.5s infinite; border-radius: 8px; }
    @keyframes loading { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    .table-row-skel { height: 60px; margin: 10px; }

    .btn-primary { background: var(--primary); color: #fff; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; display: flex; align-items: center; gap: 8px; }
    .empty-table { padding: 4rem; text-align: center; color: var(--text-muted); }
    .text-right { text-align: right; }
  `]
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
