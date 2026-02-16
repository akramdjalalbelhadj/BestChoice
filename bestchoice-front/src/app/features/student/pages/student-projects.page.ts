import { Component, inject, OnInit, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StudentService } from '../services/student.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-student-projects',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="brand">BC</div>
        <nav class="nav-links">
          <a routerLink="/app/student/dashboard" routerLinkActive="active" class="menu-item">📊 Dashboard</a>
          <a routerLink="/app/student/projects" routerLinkActive="active" class="menu-item">🔍 Projets</a>
          <a routerLink="/app/student/preferences" routerLinkActive="active" class="menu-item">⭐ Mes Choix</a>
        </nav>
        <footer class="sidebar-footer">
          <button (click)="logout()" class="btn-logout">🚪 Déconnexion</button>
        </footer>
      </aside>

      <main class="main-content">
        <header class="page-header">
          <div class="title-area">
            <a routerLink="../dashboard" class="back-link">← Retour au Dashboard</a>
            <h1>Catalogue des Projets</h1>
          </div>

          <div class="filter-bar">
            <div class="search-input">
              <input type="text" [(ngModel)]="searchQuery" placeholder="Rechercher un projet ou une techno..." />
            </div>
          </div>
        </header>

        <div class="scroll-area">
          <main class="projects-grid">
            @if (isLoading()) {
              <p>Chargement des offres du seeder...</p>
            } @else {
              @for (project of filteredProjects(); track project.id) {
                <article class="project-card">
                  <div class="card-content">
                    <span class="work-type">{{ project.workType }}</span>
                    <h3>{{ project.title }}</h3>
                    <p class="teacher">Professeur ID: {{ project.teacherId }}</p>

                    <div class="tags">
                      <span class="tag">Capacité: {{ project.maxStudents }}</span>
                      @if (project.remotePossible) {
                        <span class="tag remote">🏠 Télétravail</span>
                      }
                    </div>
                  </div>

                  <footer class="card-footer">
                    <button class="btn-outline" [routerLink]="['/app/student/projects', project.id]">Consulter</button>
                    <button class="btn-primary" (click)="addChoice(project.id)">⭐ Ajouter</button>
                  </footer>
                </article>
              } @empty {
                <div class="empty-state">Aucun projet disponible pour le moment.</div>
              }
            }
          </main>
        </div>
      </main>
    </div>
  `,
  styles: [`
    /* Reprise des variables et styles de base du Dashboard */
    .app-layout { display: flex; height: 100vh; background: #09090b; color: #fff; }
    .sidebar { width: 250px; background: #111113; border-right: 1px solid #27272a; padding: 1.5rem; display: flex; flex-direction: column; }
    .nav-links { flex: 1; display: flex; flex-direction: column; gap: 8px; margin-top: 2rem; }
    .menu-item { color: #71717a; text-decoration: none; padding: 10px; border-radius: 8px; }
    .menu-item.active { color: #3b82f6; background: rgba(59, 130, 246, 0.1); }
    .btn-logout { width: 100%; padding: 10px; background: transparent; border: 1px solid #27272a; color: #ef4444; cursor: pointer; border-radius: 8px; }

    .main-content { flex: 1; display: flex; flex-direction: column; padding: 2rem; }
    .page-header { margin-bottom: 2rem; }
    .back-link { color: #3b82f6; text-decoration: none; font-size: 0.85rem; margin-bottom: 0.5rem; display: block; }
    h1 { font-size: 1.75rem; font-weight: 800; }

    .filter-bar { margin-top: 1.5rem; }
    .search-input input { width: 100%; max-width: 400px; background: #18181b; border: 1px solid #27272a; padding: 12px; border-radius: 8px; color: #fff; }

    .projects-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1.5rem; }
    .project-card { background: #18181b; border: 1px solid #27272a; border-radius: 12px; display: flex; flex-direction: column; }
    .card-content { padding: 1.5rem; flex: 1; }
    .work-type { color: #3b82f6; font-size: 0.7rem; font-weight: 800; text-transform: uppercase; }
    h3 { margin: 10px 0; font-size: 1.1rem; }
    .tags { display: flex; gap: 8px; margin-top: 1rem; }
    .tag { background: #27272a; font-size: 0.7rem; padding: 4px 8px; border-radius: 4px; }

    .card-footer { padding: 1rem; border-top: 1px solid #27272a; display: flex; gap: 10px; }
    .btn-primary { flex: 1; background: #3b82f6; border: none; color: white; padding: 10px; border-radius: 6px; cursor: pointer; font-weight: 600; }
    .btn-outline { flex: 1; background: transparent; border: 1px solid #27272a; color: white; padding: 10px; border-radius: 6px; cursor: pointer; }
    .scroll-area { flex: 1; overflow-y: auto; margin-top: 1rem; }
  `]
})
export class StudentProjectsPage implements OnInit {
  protected readonly auth = inject(AuthStore);
  private readonly studentService = inject(StudentService);
  private readonly router = inject(Router);

  isLoading = signal(true);
  searchQuery = signal('');
  projects = this.studentService.availableProjects;

  filteredProjects = computed(() => {
    const query = this.searchQuery().toLowerCase();
    return this.projects().filter(p => p.title.toLowerCase().includes(query));
  });

  ngOnInit() {
    this.studentService.loadAvailableProjects()
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe();
  }

  addChoice(projectId: number) {
    const studentId = this.studentService.studentProfile()?.id;
    if (studentId) {
      this.studentService.addToPreferences(studentId, projectId, 1).subscribe({
        next: () => alert('Projet ajouté à vos choix !'),
        error: () => alert('Erreur lors de l\'ajout.')
      });
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
