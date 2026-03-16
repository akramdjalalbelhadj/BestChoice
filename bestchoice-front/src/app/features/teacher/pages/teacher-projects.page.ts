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
        <a routerLink="/app/teacher/dashboard" class="back-link">Retour au dashboard</a>
        <div class="title-group">
          <h1>Mes Propositions</h1>
          <p class="text-muted">Gérez vos offres et suivez l'affectation des étudiants en temps réel.</p>
        </div>
        <button class="btn-primary" routerLink="./create">
          <span class="plus">+</span> Publier un projet
        </button>
      </header>

      <section class="filter-card">
        <div class="search-box">
          <input
            type="text"
            [(ngModel)]="searchQuery"
            placeholder="Rechercher un projet par titre..."
            class="search-input"
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
                        <button class="action-btn edit" [routerLink]="['./edit', p.id]" title="Modifier">Modifier</button>
                        <button
                          class="action-btn details"
                          [routerLink]="['/app/teacher/projects', p.id]"
                          title="Voir détails">Détails</button>

                        <button class="action-btn danger" (click)="deactivate(p.id)" title="Retirer l'offre">Retirer</button>
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
    :host { --bg: #f8f9fa; --card: #ffffff; --border: #e9ecef; --primary: #007bff; --danger: #dc3545; --text-muted: #6c757d; display: block; background: var(--bg); color: #212529; min-height: 100vh; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif; }
    .page-container { padding: 3rem 2rem; max-width: 1400px; margin: 0 auto; }

    /* Header */
    .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 3rem; gap: 2rem; flex-wrap: wrap; }
    .back-link {
      color: var(--primary);
      text-decoration: none;
      font-size: 0.95rem;
      font-weight: 600;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 1.5rem;
      padding: 10px 14px;
      background: rgba(0, 123, 255, 0.08);
      border: 1.5px solid rgba(0, 123, 255, 0.25);
      border-radius: 10px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }
    .back-link::before {
      content: '←';
      font-size: 1.1rem;
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .back-link:hover {
      color: #0056cc;
      background: rgba(0, 123, 255, 0.12);
      border-color: var(--primary);
      box-shadow: 0 4px 12px rgba(0, 123, 255, 0.15);
      transform: translateY(-2px);
    }
    .back-link:hover::before {
      transform: translateX(-3px);
    }
    .back-link:active {
      transform: translateY(0);
    }
    .back-link:focus {
      outline: none;
      box-shadow: 0 0 0 4px rgba(0, 123, 255, 0.15);
    }
    .title-group { flex: 1; min-width: 0; }
    .title-group h1 { font-size: 2.5rem; font-weight: 700; margin: 0; letter-spacing: -0.02em; color: #212529; }
    .title-group p { margin: 0.5rem 0 0 0; font-size: 1.1rem; color: var(--text-muted); }

    .plus { font-size: 1.1rem; font-weight: 300; }

    /* Filters */
    .filter-card { background: var(--card); border: 1px solid var(--border); border-radius: 16px; padding: 1.5rem; margin-bottom: 2rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); }
    .search-box { position: relative; display: flex; align-items: center; }
    .search-input { width: 100%; background: #f8f9fa; border: 1.5px solid var(--border); border-radius: 10px; padding: 12px 16px; color: #212529; outline: none; font-size: 0.95rem; transition: all 0.3s ease; }
    .search-input::placeholder { color: var(--text-muted); }
    .search-input:focus { border-color: var(--primary); background: #ffffff; box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1); }

    /* Buttons */
    .btn-primary {
      background: var(--primary);
      color: white;
      border: none;
      padding: 12px 24px;
      border-radius: 10px;
      font-weight: 600;
      font-size: 0.95rem;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 8px rgba(0, 123, 255, 0.15);
      white-space: nowrap;
    }
    .btn-primary:hover {
      background: #0056cc;
      box-shadow: 0 6px 16px rgba(0, 123, 255, 0.25);
      transform: translateY(-2px);
    }
    .btn-primary:active {
      transform: translateY(0);
      box-shadow: 0 2px 4px rgba(0, 123, 255, 0.15);
    }
    .btn-primary:focus {
      outline: none;
      box-shadow: 0 0 0 4px rgba(0, 123, 255, 0.15);
    }

    /* Table */
    .table-card { background: var(--card); border: 1px solid var(--border); border-radius: 20px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }
    .table-responsive { overflow-x: auto; }
    .pro-table { width: 100%; border-collapse: collapse; font-size: 0.95rem; }
    .pro-table th { background: #f8f9fa; padding: 1.25rem 1.5rem; text-align: left; font-size: 0.85rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700; letter-spacing: 0.5px; border-bottom: 1px solid var(--border); }
    .project-row { border-bottom: 1px solid var(--border); transition: all 0.2s ease; }
    .project-row:hover { background: #f8f9fa; }
    .project-row td { padding: 1.5rem; }

    /* Project Info Cells */
    .title-cell { display: flex; flex-direction: column; gap: 8px; }
    .main-title { font-weight: 700; color: #212529; font-size: 0.95rem; }
    .keyword-list { display: flex; gap: 8px; flex-wrap: wrap; }
    .keyword-tag { font-size: 0.75rem; color: var(--primary); font-weight: 600; opacity: 0.8; }

    .type-badge { font-size: 0.75rem; font-weight: 700; background: rgba(0, 123, 255, 0.1); color: var(--primary); padding: 6px 10px; border-radius: 6px; display: inline-block; border: 1px solid rgba(0, 123, 255, 0.25); }
    .text-xs { font-size: 0.8rem; }

    .skills-stack { display: flex; flex-wrap: wrap; gap: 6px; max-width: 220px; }
    .skill-mini { font-size: 0.75rem; background: rgba(0, 123, 255, 0.08); color: var(--primary); padding: 4px 8px; border-radius: 6px; border: 1px solid rgba(0, 123, 255, 0.2); font-weight: 500; }

    /* Occupation UX */
    .capacity-visual { width: 150px; }
    .cap-text { font-size: 0.9rem; display: block; margin-bottom: 8px; color: #212529; }
    .mini-progress { height: 6px; background: #e9ecef; border-radius: 3px; overflow: hidden; }
    .fill { height: 100%; background: var(--primary); transition: width 0.6s ease; }

    /* Status & Actions */
    .status-toggle {
      background: transparent;
      border: 1.5px solid var(--border);
      color: var(--text-muted);
      padding: 9px 16px;
      border-radius: 10px;
      font-size: 0.85rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    }
    .status-toggle:hover {
      background: #f8f9fa;
      border-color: #d1d5db;
      transform: translateY(-2px);
    }
    .status-toggle:active {
      transform: translateY(0);
    }
    .status-toggle:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
    }
    .status-toggle.active {
      background: rgba(0, 123, 255, 0.1);
      color: var(--primary);
      border-color: var(--primary);
      box-shadow: 0 2px 8px rgba(0, 123, 255, 0.1);
    }
    .status-toggle.active:hover {
      background: rgba(0, 123, 255, 0.15);
      box-shadow: 0 4px 12px rgba(0, 123, 255, 0.15);
    }

    .action-menu { display: flex; gap: 8px; justify-content: flex-end; flex-wrap: wrap; }
    .action-btn {
      background: #f8f9fa;
      border: 1.5px solid var(--border);
      color: #212529;
      padding: 10px 16px;
      border-radius: 10px;
      cursor: pointer;
      font-size: 0.85rem;
      font-weight: 600;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
      white-space: nowrap;
    }
    .action-btn:hover {
      background: #ffffff;
      border-color: #d1d5db;
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
    }
    .action-btn:active {
      transform: translateY(0);
    }
    .action-btn:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
    }
    .action-btn.edit {
      background: rgba(0, 123, 255, 0.05);
      border-color: rgba(0, 123, 255, 0.2);
      color: var(--primary);
    }
    .action-btn.edit:hover {
      background: rgba(0, 123, 255, 0.12);
      border-color: var(--primary);
      color: var(--primary);
      box-shadow: 0 4px 12px rgba(0, 123, 255, 0.15);
    }
    .action-btn.details {
      background: rgba(0, 123, 255, 0.05);
      border-color: rgba(0, 123, 255, 0.2);
      color: var(--primary);
    }
    .action-btn.details:hover {
      background: rgba(0, 123, 255, 0.12);
      border-color: var(--primary);
      color: var(--primary);
      box-shadow: 0 4px 12px rgba(0, 123, 255, 0.15);
    }
    .action-btn.danger {
      background: rgba(220, 53, 69, 0.05);
      border-color: rgba(220, 53, 69, 0.2);
      color: var(--danger);
    }
    .action-btn.danger:hover {
      background: rgba(220, 53, 69, 0.12);
      border-color: var(--danger);
      color: var(--danger);
      box-shadow: 0 4px 12px rgba(220, 53, 69, 0.15);
    }

    /* Skeletons */
    .skeleton { background: linear-gradient(90deg, #e9ecef 25%, #f8f9fa 50%, #e9ecef 75%); background-size: 200% 100%; animation: loading 1.5s infinite; border-radius: 8px; }
    @keyframes loading { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    .table-row-skel { height: 70px; margin: 10px; }

    .empty-table { padding: 4rem 2rem; text-align: center; color: var(--text-muted); font-size: 1rem; }
    .text-right { text-align: right; }
    .text-muted { color: var(--text-muted); }
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
