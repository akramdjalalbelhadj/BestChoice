import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';
import { DashboardPage } from './features/dashboard/pages/dashboard.page';
import { AUTH_ROUTES } from './features/auth/auth.routes';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'app' },

  { path: 'auth', children: AUTH_ROUTES },

  {
    path: 'app',
    canActivate: [authGuard],
    children: [
      // redirect /app -> /app/{role}
      { path: '', pathMatch: 'full', component: DashboardPage },

      // ADMIN
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES),
      },

      // TEACHER
      {
        path: 'teacher',
        canActivate: [roleGuard],
        data: { roles: ['ENSEIGNANT'] },
        loadChildren: () => import('./features/teacher/teacher.routes').then(m => m.TEACHER_ROUTES),
      },

      // STUDENT
      {
        path: 'student',
        canActivate: [roleGuard],
        data: { roles: ['ETUDIANT'] },
        loadChildren: () => import('./features/student/student.routes').then(m => m.STUDENT_ROUTES),
      },
    ],
  },

  { path: '**', redirectTo: 'auth/login' }
];
