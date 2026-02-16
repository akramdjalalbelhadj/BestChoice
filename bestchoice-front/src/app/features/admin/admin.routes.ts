import { Routes } from '@angular/router';
import { AdminDashboardPage } from './pages/admin-dashboard.page';
import { AdminUsersPage } from './pages/admin-users.page';

export const ADMIN_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', component: AdminDashboardPage },
  { path: 'users', component: AdminUsersPage },
];
