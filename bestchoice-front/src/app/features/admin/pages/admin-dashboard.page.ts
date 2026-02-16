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
  styles: [`
    :host { display: block; }
    .page { padding: 24px; max-width: 1200px; margin: 0 auto; }
    .muted { opacity: 0.7; font-size: 14px; }

    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 16px;
      margin-top: 24px;
    }

    .card {
      display: block;
      padding: 20px;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      text-decoration: none;
      color: inherit;
      transition: all 0.2s ease-in-out;
    }

    .card:not(.disabled):hover {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.3);
      transform: translateY(-2px);
    }

    .card.disabled { opacity: 0.5; cursor: not-allowed; }
    .card h3 { margin: 0 0 8px 0; font-size: 18px; }

    .footer {
      margin-top: 40px;
      padding-top: 16px;
      border-top: 1px solid rgba(255, 255, 255, 0.1);
      font-size: 13px;
    }
  `]
})
export class AdminDashboardPage {
  private readonly auth = inject(AuthStore);

  readonly userInfo = computed(() =>
    `${this.auth.displayName()} (${this.auth.role()})`
  );
}
