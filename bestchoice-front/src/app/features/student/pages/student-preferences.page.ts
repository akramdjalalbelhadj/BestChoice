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
  styles: [`
    .app-layout { display: flex; height: 100vh; background: #09090b; color: #fff; }
    .sidebar { width: 250px; background: #111113; border-right: 1px solid #27272a; padding: 1.5rem; display: flex; flex-direction: column; }
    .nav-links { flex: 1; display: flex; flex-direction: column; gap: 8px; margin-top: 2rem; }
    .menu-item { color: #71717a; text-decoration: none; padding: 10px; border-radius: 8px; }
    .menu-item.active { color: #3b82f6; background: rgba(59, 130, 246, 0.1); }
    .btn-logout { width: 100%; padding: 10px; background: transparent; border: 1px solid #27272a; color: #ef4444; border-radius: 8px; cursor: pointer; text-align: left; }

    .main-content { flex: 1; padding: 2.5rem; overflow-y: auto; }
    .back-link { color: #3b82f6; text-decoration: none; font-size: 0.9rem; display: block; margin-bottom: 0.5rem; }
    h1 { font-size: 2rem; font-weight: 800; margin: 0; }

    .pref-list { display: flex; flex-direction: column; gap: 12px; margin-top: 2rem; max-width: 800px; }
    .pref-card {
      background: #18181b; border: 1px solid #27272a; border-radius: 12px;
      display: flex; align-items: center; padding: 1.25rem; gap: 20px;
      transition: transform 0.2s;
    }
    .pref-card.accepted { border-color: #4ade80; background: rgba(34, 197, 94, 0.05); }

    .rank-badge {
      width: 40px; height: 40px; background: #3b82f6; border-radius: 8px;
      display: grid; place-items: center; font-weight: 800; font-size: 1.1rem;
    }

    .pref-info { flex: 1; }
    .pref-info h3 { margin: 0 0 4px 0; font-size: 1rem; }

    .status-tag { font-size: 0.65rem; font-weight: 800; text-transform: uppercase; padding: 2px 8px; border-radius: 4px; background: #27272a; }
    .status-tag[data-status="ACCEPTED"] { color: #4ade80; background: rgba(34, 197, 94, 0.1); }
    .status-tag[data-status="REJECTED"] { color: #f87171; background: rgba(239, 68, 68, 0.1); }

    .pref-actions { display: flex; gap: 8px; }
    .btn-icon { background: #27272a; border: none; color: #fff; padding: 8px; border-radius: 6px; cursor: pointer; }
    .btn-icon.danger:hover { background: #ef4444; }

    .limit-info { margin-top: 2rem; font-size: 0.9rem; color: #71717a; border-top: 1px solid #27272a; padding-top: 1rem; }
    .empty-state { text-align: center; padding: 4rem; background: #18181b; border-radius: 16px; border: 1px dashed #27272a; }
    .btn-primary { background: #3b82f6; color: #fff; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; margin-top: 1rem; }
  `]
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
