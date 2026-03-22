import { ChangeDetectionStrategy, Component, inject, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <header>
        <h1>Admin</h1>
        <p class="muted">Supervision (MVP)</p>
      </header>

      <div class="grid">
        <a class="card" routerLink="/app/admin/users">
          <h3>Utilisateurs</h3>
          <p class="muted">Lister utilisateurs + rôles</p>
        </a>

        <div class="card disabled">
          <h3>Stats</h3>
          <p class="muted">À venir (projets, matching, etc.)</p>
        </div>
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
