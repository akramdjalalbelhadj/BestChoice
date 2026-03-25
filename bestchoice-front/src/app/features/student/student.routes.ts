import { Routes } from '@angular/router';
import { StudentDashboardPage } from './pages/dashboard/student-dashboard.page';
import { StudentPreferencesPage } from './pages/preference/student-preferences.page';
import {StudentProfilePage} from './pages/profil/student-profile.page';
import { StudentCampaignPage } from './pages/campaign/student-campaign.page';
import {StudentItemDetailPage} from './pages/item/student-item-detail.page';

export const STUDENT_ROUTES: Routes = [
  {
    path: '',
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },

      // Page d'accueil avec KPIs et Top Matching
      { path: 'dashboard', component: StudentDashboardPage },

      // Gestion du classement des vœux (Rank 1-10)
      { path: 'preferences', component: StudentPreferencesPage },

      // Édition du profil (Skills/Interests)
      { path: 'profile', component: StudentProfilePage },

      { path: 'campaigns', component: StudentCampaignPage },

      {path: 'items/:id', component: StudentItemDetailPage}
    ]
  }
];
