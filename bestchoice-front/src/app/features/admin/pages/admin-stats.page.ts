import {
  Component, inject, signal, computed,
  ChangeDetectionStrategy, ChangeDetectorRef, OnInit
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AdminService, AdminStats } from '../services/admin.service';
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

  stats     = signal<AdminStats | null>(null);
  isLoading = signal(true);
  errorMsg  = signal<string | null>(null);

  projOpen = signal(true);
  subjOpen = signal(true);

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
    this.adminService.getStats().subscribe({
      next: s  => { this.stats.set(s); this.isLoading.set(false); this.cdr.markForCheck(); },
      error: () => { this.errorMsg.set('Impossible de charger les statistiques.'); this.isLoading.set(false); this.cdr.markForCheck(); }
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
