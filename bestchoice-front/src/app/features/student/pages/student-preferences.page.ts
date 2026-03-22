import { Component, inject, OnInit, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { StudentService } from '../services/student.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { PreferenceResponse } from '../models/preference.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-student-preferences',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
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
          <a routerLink="../dashboard" class="back-link">← Retour au Dashboard</a>
          <h1>Mes Vœux Classés</h1>
          <p class="text-muted">Vous pouvez classer jusqu'à 10 projets par ordre de priorité.</p>
        </header>

        <div class="preferences-container">
          @if (isLoading()) {
            <p>Chargement de vos choix...</p>
          } @else {
            <div class="pref-list">
              @for (pref of preferences(); track pref.id) {
                <div class="pref-card" [class.accepted]="pref.status === 'ACCEPTED'">
                  <div class="rank-badge">#{{ pref.rank }}</div>

                  <div class="pref-info">
                    <h3>Projet ID: {{ pref.projectId }}</h3>
                    <span class="status-tag" [attr.data-status]="pref.status">
                      {{ pref.status }}
                    </span>
                    <p class="text-xs text-muted">Ajouté le {{ pref.createdAt | date:'shortDate' }}</p>
                  </div>

                  <div class="pref-actions">
                    <button class="btn-icon" [routerLink]="['/app/student/projects', pref.projectId]">👁️</button>
                    @if (pref.status === 'PENDING') {
                      <button class="btn-icon danger" (click)="removePreference(pref.id)">🗑️</button>
                    }
                  </div>
                </div>
              } @empty {
                <div class="empty-state">
                  <p>Vous n'avez pas encore exprimé de vœux.</p>
                  <button class="btn-primary" routerLink="/app/student/projects">Explorer le catalogue</button>
                </div>
              }
            </div>

            @if (preferences().length > 0) {
              <div class="limit-info">
                {{ preferences().length }} / 10 vœux utilisés
              </div>
            }
          }
        </div>
      </main>
    </div>
  `,
  styleUrl: './student-preferences.page.scss'
})
export class StudentPreferencesPage implements OnInit {
  private readonly studentService = inject(StudentService);
  protected readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  preferences = signal<PreferenceResponse[]>([]);
  isLoading = signal(true);

  ngOnInit() {
    const user = this.auth.user();

    if (user?.userId) {
      // 🛡️ SÉCURITÉ : On s'assure d'avoir le profil avant de charger les vœux
      this.studentService.loadProfile(user.userId).subscribe({
        next: (student) => {
          if (student.id) this.fetchPreferences(student.id);
        },
        error: () => this.isLoading.set(false)
      });
    }
  }

  private fetchPreferences(studentId: number) {
    this.studentService.getPreferences(studentId)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe(res => {
        // On trie par rang côté front pour être sûr de l'affichage
        const sorted = res.sort((a, b) => a.rank - b.rank);
        this.preferences.set(sorted);
      });
  }

  removePreference(id: number) {
    if (confirm('Voulez-vous vraiment supprimer ce vœu ? cette action est irréversible.')) {
      this.studentService.deletePreference(id).subscribe({
        next: () => {
          this.preferences.update(list => list.filter(p => p.id !== id));
        },
        error: (err) => alert("Impossible de supprimer : le vœu n'est peut-être plus en attente.")
      });
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
