import { Routes } from '@angular/router';
import { AdminDashboardPage } from './pages/admin-dashboard.page';
import { AdminUsersPage } from './pages/admin-users.page';
import { AdminUserCreatePage } from './pages/admin-user-create.page';
import { AdminStatsPage } from './pages/admin-stats.page';

export const ADMIN_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: AdminDashboardPage },
  { path: 'users', component: AdminUsersPage },
  { path: 'users/create', component: AdminUserCreatePage },
  { path: 'stats', component: AdminStatsPage },
];
