import { Routes } from '@angular/router';
import { TeacherDashboardPage } from './pages/teacher-dashboard.page';
import { TeacherProjectsPage } from './pages/teacher-projects.page';
import { ProjectCreatePage } from './pages/project-create.page';
import { MatchingControlPage } from './pages/matching-control.page';

export const TEACHER_ROUTES: Routes = [
  {
    path: '',
    children: [
      // Redirection par défaut vers le dashboard
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },

      // Page d'accueil (KPIs + Top Matching)
      { path: 'dashboard', component: TeacherDashboardPage },

      // Gestion de la liste des projets
      { path: 'projects', component: TeacherProjectsPage },

      // LA CIBLE DU BOUTON : Le formulaire de création
      { path: 'projects/create', component: ProjectCreatePage },

      // (Optionnel) Formulaire d'édition
      { path: 'projects/edit/:id', component: ProjectCreatePage },

      // Page de pilotage du matching (ajustement des poids + lancement)
      { path: 'matching-control', component: MatchingControlPage }
    ]
  }
];
