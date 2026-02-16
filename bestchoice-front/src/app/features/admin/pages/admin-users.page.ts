import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page">
      <div class="topbar">
        <h1>Utilisateurs</h1>
        <a class="link" routerLink="/app/admin/dashboard">← Retour</a>
      </div>

      <div class="card">
        <p class="muted">
          MVP: ici on affichera la table paginée via <code>GET /api/users/paginated</code>.
        </p>
      </div>
    </div>
  `,
  styles: [`
    .page{padding:16px}
    .topbar{display:flex;align-items:center;justify-content:space-between;gap:12px}
    .muted{opacity:.75}
    .link{font-size:13px;opacity:.85}
    .card{margin-top:12px;border:1px solid rgba(255,255,255,.12);border-radius:16px;padding:16px;background:rgba(255,255,255,.04)}
    code{opacity:.9}
  `]
})
export class AdminUsersPage {}
