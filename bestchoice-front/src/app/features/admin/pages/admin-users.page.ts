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
        <a class="btn-retour" routerLink="/app/admin/dashboard">← Retour</a>
      </div>

      <div class="card">
        <p class="muted">
          MVP: ici on affichera la table paginée via <code>GET /api/users/paginated</code>.
        </p>
      </div>
    </div>
  `,
  styleUrl: './admin-users.page.scss'
})
export class AdminUsersPage {}
