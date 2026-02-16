import { Routes } from '@angular/router';
import { StudentDashboardPage } from './pages/student-dashboard.page';
import { StudentProjectsPage } from './pages/student-projects.page';
import { StudentProjectDetailPage } from './pages/student-project-detail.page'; // ➕ Import
import { StudentPreferencesPage } from './pages/student-preferences.page';
import {StudentProfilePage} from './pages/student-profile.page';     // ➕ Import

export const STUDENT_ROUTES: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },

      // Page d'accueil avec KPIs et Top Matching
      { path: 'dashboard', component: StudentDashboardPage },

      // Catalogue complet des projets
      { path: 'projects', component: StudentProjectsPage },

      // Détails d'un projet spécifique + Formulaire de vœux
      { path: 'projects/:id', component: StudentProjectDetailPage },

      // Gestion du classement des vœux (Rank 1-10)
      { path: 'preferences', component: StudentPreferencesPage },

      // Édition du profil (Skills/Interests)
      { path: 'profile', component: StudentProfilePage },
    ]
  }
];
