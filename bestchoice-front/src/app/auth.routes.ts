import { Routes } from '@angular/router';
import { AUTH_ROUTES } from './features/auth/auth.routes';

export const APP_ROUTES: Routes = [
  { path: 'auth', children: AUTH_ROUTES },
  { path: '', pathMatch: 'full', redirectTo: 'auth/login' },
  { path: '**', redirectTo: 'auth/login' }
];
