import { Routes } from '@angular/router';
import { LoginPage } from './features/auth/pages/login.page';
import { RegisterPage } from './features/auth/pages/register.page';
import { ForgotPasswordPage } from './features/auth/pages/forgot-password.page';
import { DashboardPage } from './features/dashboard/pages/dashboard.page';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'auth/login' },

  { path: 'auth/login', component: LoginPage },
  { path: 'auth/register', component: RegisterPage },
  { path: 'auth/forgot-password', component: ForgotPasswordPage },
  { path: 'dashboard', component: DashboardPage },

  { path: '**', redirectTo: 'auth/login' }
];
