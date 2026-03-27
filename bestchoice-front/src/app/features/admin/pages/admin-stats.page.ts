import {
  Component, inject, signal, computed,
  ChangeDetectionStrategy, ChangeDetectorRef, OnInit
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AdminService, AdminStats, ProjectSummary, TeacherProjectGroup, SubjectSummary, TeacherSubjectGroup } from '../services/admin.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../shared/theme-toggle.component';

interface BarEntry { label: string; value: number; pct: number; }

const WORK_TYPE_LABELS: Record<string, string> = {
  DEVELOPPEMENT: 'Développement',
  RECHERCHE: 'Recherche',
  ANALYSE: 'Analyse',
  CONCEPTION: 'Conception',
};

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-stats.page.html',
  styleUrl:    './admin-stats.page.scss'
})
export class AdminStatsPage implements OnInit {
  private readonly adminService = inject(AdminService);
  protected readonly auth       = inject(AuthStore);
  private readonly router       = inject(Router);
  private readonly cdr          = inject(ChangeDetectorRef);

  stats       = signal<AdminStats | null>(null);
  allProjects = signal<ProjectSummary[]>([]);
  allSubjects = signal<SubjectSummary[]>([]);
  isLoading   = signal(true);
  errorMsg    = signal<string | null>(null);

  activeTab       = signal<'projects' | 'subjects'>('projects');
  teacherProjOpen = signal(true);
  teacherSubjOpen = signal(true);

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  // ── Donuts ───────────────────────────────────────────────────────────────

  projActivePct = computed(() => {
    const s = this.stats(); if (!s || s.totalProjects === 0) return 0;
    return Math.round(s.activeProjects / s.totalProjects * 100);
  });

  subjActivePct = computed(() => {
    const s = this.stats(); if (!s || s.totalSubjects === 0) return 0;
    return Math.round(s.activeSubjects / s.totalSubjects * 100);
  });

  projCompletedPct = computed(() => {
    const s = this.stats(); if (!s || s.totalProjects === 0) return 0;
    return Math.round(s.completedProjects / s.totalProjects * 100);
  });

  // ── Bar charts ───────────────────────────────────────────────────────────

  projWorkTypeBars = computed(() => this.toBars(this.stats()?.projectsByWorkType ?? {}, true));
  subjWorkTypeBars = computed(() => this.toBars(this.stats()?.subjectsByWorkType ?? {}, true));

  projSemesterBars = computed(() => this.toBars(this.stats()?.projectsBySemester ?? {}, false));
  subjSemesterBars = computed(() => this.toBars(this.stats()?.subjectsBySemester ?? {}, false));

  teacherProjectGroups = computed<TeacherProjectGroup[]>(() => {
    const projects = this.allProjects();
    const map = new Map<string, TeacherProjectGroup>();
    for (const p of projects) {
      const key = p.teacherName ?? 'Inconnu';
      if (!map.has(key)) map.set(key, { teacherName: key, teacherId: p.teacherId, projects: [] });
      map.get(key)!.projects.push(p);
    }
    return Array.from(map.values()).sort((a, b) => b.projects.length - a.projects.length);
  });

  teacherSubjectGroups = computed<TeacherSubjectGroup[]>(() => {
    const subjects = this.allSubjects();
    const map = new Map<string, TeacherSubjectGroup>();
    for (const s of subjects) {
      const key = s.teacherName ?? 'Inconnu';
      if (!map.has(key)) map.set(key, { teacherName: key, teacherId: s.teacherId, subjects: [] });
      map.get(key)!.subjects.push(s);
    }
    return Array.from(map.values()).sort((a, b) => b.subjects.length - a.subjects.length);
  });

  projTeacherBars = computed(() => {
    const entries = this.stats()?.topTeachersByProjects ?? [];
    const max = Math.max(...entries.map(e => e.count), 1);
    return entries.map(e => ({ label: e.name, value: e.count, pct: Math.round(e.count / max * 100) }));
  });

  subjTeacherBars = computed(() => {
    const entries = this.stats()?.topTeachersBySubjects ?? [];
    const max = Math.max(...entries.map(e => e.count), 1);
    return entries.map(e => ({ label: e.name, value: e.count, pct: Math.round(e.count / max * 100) }));
  });

  // ── Lifecycle ────────────────────────────────────────────────────────────

  ngOnInit() { this.load(); }

  private load() {
    this.isLoading.set(true);
    forkJoin({
      stats:    this.adminService.getStats(),
      projects: this.adminService.getAllProjects(),
      subjects: this.adminService.getAllSubjects()
    }).subscribe({
      next: ({ stats, projects, subjects }) => {
        this.stats.set(stats);
        this.allProjects.set(projects);
        this.allSubjects.set(subjects);
        this.isLoading.set(false);
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMsg.set('Impossible de charger les statistiques.');
        this.isLoading.set(false);
        this.cdr.markForCheck();
      }
    });
  }

  private toBars(map: Record<string, number>, translateKey: boolean): BarEntry[] {
    const entries = Object.entries(map).sort(([, a], [, b]) => b - a);
    const max = Math.max(...entries.map(([, v]) => v), 1);
    return entries.map(([k, v]) => ({
      label: translateKey ? (WORK_TYPE_LABELS[k] ?? k) : k,
      value: v,
      pct: Math.round(v / max * 100)
    }));
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
