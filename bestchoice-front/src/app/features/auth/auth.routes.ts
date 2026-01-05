import { Routes } from '@angular/router';
import { guestGuard } from '../../core/auth/guest.guard';
import { LoginPage } from './pages/login.page';
import { RegisterPage } from './pages/register.page';
import { ForgotPasswordPage } from './pages/forgot-password.page';

export const AUTH_ROUTES: Routes = [
  { path: 'login', canActivate: [guestGuard], component: LoginPage },
  { path: 'register', canActivate: [guestGuard], component: RegisterPage },
  { path: 'forgot-password', canActivate: [guestGuard], component: ForgotPasswordPage }
];
