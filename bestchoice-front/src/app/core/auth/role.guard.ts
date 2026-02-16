import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthStore } from './auth.store';
import type { Role } from '../models/auth.model';

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthStore);
  const router = inject(Router);

  const allowed = (route.data?.['roles'] as Role[] | undefined) ?? [];
  const role = auth.role();

  if (!auth.isAuthenticated()) {
    router.navigateByUrl('/auth/login');
    return false;
  }

  if (!role) {
    router.navigateByUrl('/auth/login');
    return false;
  }

  if (allowed.length === 0 || allowed.includes(role)) return true;

  router.navigateByUrl(auth.homeUrlForRole(role));
  return false;
};
