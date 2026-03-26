import { Component, inject, signal, computed, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CampaignService } from '../../../campaign/services/campaign.service';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../../shared/theme-toggle.component';
import { MatchingAlgorithmType } from '../../../../core/models/enums.model';
import { MatchingCampaignType } from '../../../campaign/models/matching-campaign-type.model';
import { finalize } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { CampaignRequest } from '../../../campaign/models/campaign.model';

@Component({
  selector: 'app-campaign-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, RouterLinkActive, ThemeToggleComponent],
  templateUrl: './teacher-campaign-create.page.html',
  styleUrl: './teacher-campaign-create.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TeacherCampaignCreatePage implements OnInit {
  private readonly fb             = inject(FormBuilder);
  private readonly campaignService = inject(CampaignService);
  private readonly teacherService  = inject(TeacherService);
  protected readonly auth          = inject(AuthStore);
  private readonly router          = inject(Router);
  private readonly cdr             = inject(ChangeDetectorRef);

  isSubmitting = signal(false);

  // ── Signaux d'erreur ────────────────────────────────────────────────────────
  nameError          = signal(false);
  yearEmptyError     = signal(false);
  yearFormatError    = signal(false);
  studentsError      = signal(false);
  itemsError         = signal(false);

  private readonly academicYearPattern = /^\d{4}-\d{4}$/;

  availableStudents = this.teacherService.allStudents;
  availableProjects = computed(() => this.teacherService.projects().filter(p => p.active));
  availableSubjects = computed(() => this.teacherService.subjects().filter(s => s.active));

  selectedStudentIds = signal<number[]>([]);
  selectedItemIds    = signal<number[]>([]);

  studentSearch = signal('');
  itemSearch    = signal('');

  filteredStudents = computed(() => {
    const q = this.studentSearch().toLowerCase().trim();
    if (!q) return this.availableStudents();
    return this.availableStudents().filter(s =>
      `${s.firstName} ${s.lastName}`.toLowerCase().includes(q) ||
      s.email.toLowerCase().includes(q)
    );
  });

  filteredItems = computed(() => {
    const q = this.itemSearch().toLowerCase().trim();
    if (this.isProjectType()) {
      if (!q) return this.availableProjects();
      return this.availableProjects().filter(p => p.title.toLowerCase().includes(q));
    } else {
      if (!q) return this.availableSubjects();
      return this.availableSubjects().filter(s => s.title.toLowerCase().includes(q));
    }
  });

  algorithmTypes = [
    MatchingAlgorithmType.WEIGHTED,
    MatchingAlgorithmType.STABLE
  ];
  campaignTypes = [
    { label: 'Projets (PFE/Tutorés)', value: MatchingCampaignType.PROJECT },
    { label: 'Matières / Options', value: MatchingCampaignType.SUBJECT }
  ];

  form = this.fb.group({
    name: ['', [Validators.required]],
    description: [''],
    academicYear: ['2025-2026', [Validators.required, Validators.pattern(/^\d{4}-\d{4}$/)]],
    semester: [1, [Validators.required]],
    campaignType: [MatchingCampaignType.PROJECT, [Validators.required]],
    algorithmType: [MatchingAlgorithmType.WEIGHTED, [Validators.required]],
    skillsWeight: [34],
    interestsWeight: [33],
    workTypeWeight: [33]
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  ngOnInit() {
    const userId = this.auth.user()?.userId;
    if (userId) {
      this.teacherService.loadAllData(userId);
    }

    // Effacement des erreurs dès que l'utilisateur corrige
    this.form.get('name')!.valueChanges.subscribe(v => {
      if (v?.trim()) { this.nameError.set(false); this.cdr.markForCheck(); }
    });
    this.form.get('academicYear')!.valueChanges.subscribe(v => {
      const val = v?.trim() ?? '';
      if (val) this.yearEmptyError.set(false);
      if (this.academicYearPattern.test(val)) this.yearFormatError.set(false);
      this.cdr.markForCheck();
    });
  }

  campaignTypeSelected = toSignal(
    this.form.get('campaignType')!.valueChanges,
    { initialValue: MatchingCampaignType.PROJECT }
  );

  isProjectType = computed(() => this.campaignTypeSelected() === MatchingCampaignType.PROJECT);

  onTypeChange() {
    this.selectedItemIds.set([]);
    this.itemSearch.set('');
  }

  updateWeight(field: 'skillsWeight' | 'interestsWeight' | 'workTypeWeight', event: Event) {
    const newVal = +(event.target as HTMLInputElement).value;
    const oldVal = this.form.get(field)?.value || 0;
    const diff = newVal - oldVal;
    const otherFields = (['skillsWeight', 'interestsWeight', 'workTypeWeight'] as const).filter(f => f !== field);

    let nextF1 = (this.form.get(otherFields[0])?.value || 0) - (diff / 2);
    let nextF2 = (this.form.get(otherFields[1])?.value || 0) - (diff / 2);

    if (nextF1 < 0) { nextF2 += nextF1; nextF1 = 0; }
    if (nextF2 < 0) { nextF1 += nextF2; nextF2 = 0; }
    if (nextF1 > 100) nextF1 = 100;
    if (nextF2 > 100) nextF2 = 100;

    this.form.patchValue({ [field]: newVal, [otherFields[0]]: Math.round(nextF1), [otherFields[1]]: Math.round(nextF2) }, { emitEvent: false });

    const finalTotal = (this.form.get('skillsWeight')?.value || 0) +
      (this.form.get('interestsWeight')?.value || 0) +
      (this.form.get('workTypeWeight')?.value || 0);
    if (finalTotal !== 100) {
      const current = this.form.get(otherFields[1])?.value || 0;
      this.form.get(otherFields[1])?.setValue(current + (100 - finalTotal), { emitEvent: false });
    }
  }

  toggleStudent(id: number) {
    this.selectedStudentIds.update(ids => ids.includes(id) ? ids.filter(i => i !== id) : [...ids, id]);
    if (this.selectedStudentIds().length > 0) { this.studentsError.set(false); this.cdr.markForCheck(); }
  }

  toggleItem(id: number) {
    this.selectedItemIds.update(ids => ids.includes(id) ? ids.filter(i => i !== id) : [...ids, id]);
    if (this.selectedItemIds().length > 0) { this.itemsError.set(false); this.cdr.markForCheck(); }
  }

  selectAllStudents() {
    const allIds = this.availableStudents().map(s => s.id);
    this.selectedStudentIds.set(this.selectedStudentIds().length === allIds.length ? [] : allIds);
    if (this.selectedStudentIds().length > 0) { this.studentsError.set(false); this.cdr.markForCheck(); }
  }

  onSubmit() {
    const raw = this.form.getRawValue();
    const yearVal     = raw.academicYear?.trim() ?? '';
    const nameOk      = !!raw.name?.trim();
    const yearNotEmpty = yearVal.length > 0;
    const yearFmtOk   = this.academicYearPattern.test(yearVal);
    const studentsOk  = this.selectedStudentIds().length > 0;
    const itemsOk     = this.selectedItemIds().length > 0;

    this.nameError.set(!nameOk);
    this.yearEmptyError.set(!yearNotEmpty);
    this.yearFormatError.set(yearNotEmpty && !yearFmtOk);
    this.studentsError.set(!studentsOk);
    this.itemsError.set(!itemsOk);
    this.cdr.markForCheck();

    if (!nameOk || !yearNotEmpty || !yearFmtOk || !studentsOk || !itemsOk) return;

    this.isSubmitting.set(true);
    const payload: CampaignRequest = {
      name: raw.name ?? '',
      description: raw.description ?? '',
      academicYear: raw.academicYear ?? '',
      semester: Number(raw.semester),
      campaignType: raw.campaignType!,
      algorithmType: raw.algorithmType!,
      skillsWeight: (raw.skillsWeight ?? 0) / 100,
      interestsWeight: (raw.interestsWeight ?? 0) / 100,
      workTypeWeight: (raw.workTypeWeight ?? 0) / 100,
      teacherId: this.auth.user()?.userId!,
      studentIds: this.selectedStudentIds(),
      projectIds: this.isProjectType() ? this.selectedItemIds() : [],
      subjectIds: !this.isProjectType() ? this.selectedItemIds() : []
    };
    this.teacherService.createCompleteCampaign(payload)
      .pipe(finalize(() => this.isSubmitting.set(false)))
      .subscribe({
        next: () => this.router.navigate(['/app/teacher/dashboard']),
        error: (err) => { console.error('Erreur publication campagne :', err); alert('Une erreur est survenue lors de la création.'); }
      });
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
