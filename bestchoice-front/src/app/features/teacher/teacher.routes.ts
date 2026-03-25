import { Routes } from '@angular/router';
import { TeacherDashboardPage } from './pages/dashboard/teacher-dashboard.page';
import { TeacherProjectsPage } from './pages/project/teacher-projects.page';
import { TeacherProjectFormPage } from './pages/project/teacher-project-create.page';
import { TeacherMatchingControlPage } from './pages/matching/teacher-matching-control.page';
import { TeacherProjectDetailPage } from './pages/project/teacher-project-detail.page';
import { TeacherCampaignCreatePage } from './pages/campaign/teacher-campaign-create.page';
import { TeacherCampaignsPage } from './pages/campaign/teacher-campaigns.page';
import { MatchingResultsViewPage } from './pages/matching/teacher-matching.page';
import { TeacherSubjectsPage } from './pages/subject/teacher-subjects.page';
import { TeacherSubjectFormPage } from './pages/subject/teacher-subject-form.page';

export const TEACHER_ROUTES: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', component: TeacherDashboardPage },

      // --- SECTION PROJETS ---
      { path: 'projects', component: TeacherProjectsPage },
      { path: 'projects/create', component: TeacherProjectFormPage },
      { path: 'projects/edit/:id', component: TeacherProjectFormPage },
      { path: 'projects/:id', component: TeacherProjectDetailPage },

      // --- SECTION OPTIONS (SUBJECTS) ---
      { path: 'subjects', component: TeacherSubjectsPage },
      { path: 'subjects/create', component: TeacherSubjectFormPage },
      { path: 'subjects/edit/:id', component: TeacherSubjectFormPage },

      // --- SECTION CAMPAGNES ---
      { path: 'campaigns', component: TeacherCampaignsPage },
      { path: 'campaigns/create', component: TeacherCampaignCreatePage },
      { path: 'campaigns/results/:id', component: MatchingResultsViewPage },

      // --- SECTION MATCHING ---
      { path: 'matching-control', component: TeacherMatchingControlPage }

    ]
  }
];
