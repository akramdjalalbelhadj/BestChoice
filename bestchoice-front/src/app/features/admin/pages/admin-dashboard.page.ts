import { ChangeDetectionStrategy, Component, inject, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthStore } from '../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../shared/theme-toggle.component';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header>
        <div class="header-row">
          <div>
            <h1>Admin</h1>
            <p class="muted">Supervision (MVP)</p>
          </div>
          <app-theme-toggle />
        </div>
      </header>

      <div class="grid">
        <a class="card" routerLink="/app/admin/users">
          <h3>Utilisateurs</h3>
          <p class="muted">Lister utilisateurs + rôles</p>
        </a>

        <a class="card" routerLink="/app/admin/stats">
          <h3>Statistiques</h3>
          <p class="muted">Projets, options, enseignants, étudiants</p>
        </a>
      </div>

      <footer class="footer muted">
        Connecté : <strong>{{ userInfo() }}</strong>
      </footer>
    </div>
  `,
  styleUrl: './admin-dashboard.page.scss'
})
export class AdminDashboardPage {
  private readonly auth = inject(AuthStore);

  readonly userInfo = computed(() =>
    `${this.auth.displayName()} (${this.auth.role()})`
  );
}
