import { Routes } from '@angular/router';
import { TeacherDashboardPage } from './pages/dashboard/teacher-dashboard.page';
import { TeacherProjectsPage } from './pages/project/teacher-projects.page';
import { ProjectCreatePage } from './pages/project/project-create.page';
import { MatchingControlPage } from './pages/matching control/matching-control.page';
import { CampaignCreatePage } from './pages/campaign/campaign-create.page';

export const TEACHER_ROUTES: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: TeacherDashboardPage },

      // --- SECTION PROJETS ---
      { path: 'projects', component: TeacherProjectsPage },
      { path: 'projects/create', component: ProjectCreatePage },
      { path: 'projects/edit/:id', component: ProjectCreatePage },

      // --- SECTION CAMPAGNES ---
      { path: 'campaigns/create', component: CampaignCreatePage },

      // --- SECTION MATCHING ---
      { path: 'matching-control', component: MatchingControlPage }
    ]
  }
];
