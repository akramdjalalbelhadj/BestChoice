import { Component, inject, OnInit, ChangeDetectionStrategy, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TeacherService } from '../services/teacher.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { finalize } from 'rxjs';
import { MatchingService } from '../../../core/services/matching.service';
import { MatchingResultResponse } from '../../matching/models/matching.model';

@Component({
  selector: 'app-teacher-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="dashboard-container">
      <header class="dashboard-header">
        <div class="welcome-text">
          <h1>Tableau de bord</h1>
          <p class="text-muted">Suivi des affectations et gestion des algorithmes.</p>
        </div>

        <div class="header-actions">
          <button class="btn-outline-primary" routerLink="../matching-control">
            <span class="icon">⚙️</span> Configurer Matching
          </button>

          <button class="btn-primary" routerLink="../projects/create">
            <span class="icon">＋</span> Nouveau Projet
          </button>

          <button class="btn-logout" (click)="logout()" title="Se déconnecter">
            <span class="icon">🚪</span>
          </button>
        </div>
      </header>

      <section class="kpi-grid">
        <div class="kpi-card">
          <label>Projets Actifs</label>
          <div class="value-container">
            @if (isLoading()) { <div class="skeleton kpi-val"></div> }
            @else { <span class="value">{{ projects().length }}</span> }
          </div>
        </div>
        <div class="kpi-card">
          <label>Capacité Totale</label>
          <div class="value-container">
            @if (isLoading()) { <div class="skeleton kpi-val"></div> }
            @else { <span class="value">{{ totalCapacity() }}</span> }
          </div>
        </div>
        <div class="kpi-card highlight">
          <label>Taux de Remplissage</label>
          <div class="value-container">
            @if (isLoading()) { <div class="skeleton kpi-val"></div> }
            @else { <span class="value">{{ occupancyRate() }}%</span> }
          </div>
        </div>
      </section>

      <div class="main-layout">
        <section class="card content-left">
          <div class="card-header">
            <h3>Mes Propositions Actives</h3>
            <a routerLink="../projects" class="text-link">Gérer tout</a>
          </div>

          <div class="project-list">
            @if (isLoading()) {
              @for (i of [1,2,3]; track i) { <div class="skeleton project-item-skel"></div> }
            } @else {
              @for (p of projects(); track p.id) {
                <div class="project-item">
                  <div class="item-info">
                    <h4>{{ p.title }}</h4>
                    <p class="text-xs text-muted">
                      {{ p.workType }} •
                      <strong>{{ p.assignedStudentEmails?.length || 0 }} / {{ p.maxStudents }}</strong> places
                    </p>
                  </div>
                  <div class="item-visual">
                    <div class="progress-track">
                      <div class="progress-fill"
                           [style.width.%]="((p.assignedStudentEmails?.length || 0) / p.maxStudents) * 100">
                      </div>
                    </div>
                  </div>
                </div>
              } @empty {
                <div class="empty-state">
                  <div class="empty-icon">📂</div>
                  <p>Aucune offre publiée pour le moment.</p>
                  <button class="btn-outline-sm" routerLink="../projects/create">Créer un projet</button>
                </div>
              }
            }
          </div>
        </section>

        <section class="card content-right">
          <div class="card-header">
            <h3>Top Candidats (Matching)</h3>
            <span class="badge-beta">IA</span>
          </div>
          <div class="match-stack">
            @if (isLoadingMatches()) {
              @for (i of [1,2,3,4]; track i) { <div class="skeleton match-skel"></div> }
            } @else {
              @for (match of topGlobalMatches(); track match.id) {
                <div class="match-item-pro">
                  <div class="score">{{ (match.globalScore * 100).toFixed(0) }}%</div>
                  <div class="m-info">
                    <span class="m-name">Étudiant #{{ match.studentId }}</span>
                    <span class="m-tag">Cible : Projet {{ match.projectId }}</span>
                  </div>
                </div>
              } @empty {
                <div class="empty-state-sm">
                  <p class="text-muted">Aucun score disponible. Lancez une session de matching.</p>
                </div>
              }
            }
          </div>
        </section>
      </div>
    </main>
  `,
  styleUrl: './teacher-dashboard.page.scss'
})
export class TeacherDashboardPage implements OnInit {
  private teacherService = inject(TeacherService);
  private matchingService = inject(MatchingService);
  private auth = inject(AuthStore);
  private router = inject(Router);

  isLoading = signal(true);
  isLoadingMatches = signal(true);

  projects = this.teacherService.projects;
  topGlobalMatches = signal<MatchingResultResponse[]>([]);

  totalCapacity = computed(() =>
    this.projects().reduce((acc, p) => acc + (p.maxStudents || 0), 0)
  );

  occupancyRate = computed(() => {
    const total = this.totalCapacity();
    if (total === 0) return 0;
    const assigned = this.projects().reduce((acc, p) => acc + (p.assignedStudentEmails?.length || 0), 0);
    return Math.round((assigned / total) * 100);
  });

  ngOnInit() {
    const user = this.auth.user();
    if (user?.userId) {
      this.teacherService.loadMyProjects(user.userId)
        .pipe(finalize(() => this.isLoading.set(false)))
        .subscribe(myProjects => {
          if (myProjects && myProjects.length > 0) {
            this.loadMatchingInsights(myProjects[0].id);
          } else {
            this.isLoadingMatches.set(false);
          }
        });
    }
  }

  private loadMatchingInsights(projectId: number) {
    this.matchingService.getResultsByProject(projectId)
      .pipe(finalize(() => this.isLoadingMatches.set(false)))
      .subscribe({
        next: (res) => this.topGlobalMatches.set(res.slice(0, 5)),
        error: (err) => {
          console.error("Erreur de récupération des matchs :", err);
          this.topGlobalMatches.set([]);
        }
      });
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
