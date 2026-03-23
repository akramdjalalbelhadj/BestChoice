import { Component, inject, OnInit, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StudentService } from '../services/student.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { finalize } from 'rxjs';
import { ThemeToggleComponent } from '../../../shared/theme-toggle.component';

@Component({
  selector: 'app-student-projects',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="app-layout">
      <aside class="sidebar">
        <div class="brand">BC</div>
        <nav class="nav-links">
          <a routerLink="/app/student/dashboard" routerLinkActive="active" class="menu-item">📊 Dashboard</a>
          <a routerLink="/app/student/projects" routerLinkActive="active" class="menu-item">🔍 Projets</a>
          <a routerLink="/app/student/preferences" routerLinkActive="active" class="menu-item">⭐ Mes Choix</a>
          <a routerLink="/app/student/profile" routerLinkActive="active" class="menu-item">👤 Mon Profil</a>
        </nav>
        <footer class="sidebar-footer">
          <app-theme-toggle />
          <button (click)="logout()" class="btn-logout">🚪 Déconnexion</button>
        </footer>
      </aside>

      <main class="main-content">
        <header class="page-header">
          <div class="title-area">
            <a routerLink="../dashboard" class="btn-retour">← Retour au Dashboard</a>
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
  styleUrl: './student-projects.page.scss'
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
