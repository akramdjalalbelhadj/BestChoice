import { Component, inject, signal, computed, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CampaignService } from '../../../campaign/services/campaign.service';
import { TeacherService } from '../../services/teacher.service';
import { AuthStore } from '../../../../core/auth/auth.store';
import { MatchingAlgorithmType } from '../../../matching/models/matching.model';
import { MatchingCampaignType } from '../../../campaign/models/matching-campaign-type.model';
import { finalize, forkJoin, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-campaign-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './campaign-create.page.html',
  styleUrl: './campaign-create.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CampaignCreatePage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly campaignService = inject(CampaignService);
  private readonly teacherService = inject(TeacherService);
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  isSubmitting = signal(false);

  availableStudents = this.teacherService.allStudents;
  availableProjects = this.teacherService.projects;
  availableSubjects = this.teacherService.subjects;

  selectedStudentIds = signal<number[]>([]);
  selectedItemIds = signal<number[]>([]);

  algorithmTypes: MatchingAlgorithmType[] = ['WEIGHTED', 'STABLE', 'HYBRID'];
  campaignTypes = [
    { label: 'Projets (PFE/Tutorés)', value: MatchingCampaignType.PROJECT },
    { label: 'Matières / Options', value: MatchingCampaignType.SUBJECT }
  ];

  form = this.fb.group({
    name: ['', [Validators.required]],
    description: [''],
    academicYear: ['2025-2026', [Validators.required]],
    semester: [1, [Validators.required]],
    campaignType: [MatchingCampaignType.PROJECT, [Validators.required]],
    algorithmType: ['HYBRID' as MatchingAlgorithmType, [Validators.required]],
    skillsWeight: [34],
    interestsWeight: [33],
    workTypeWeight: [33]
  });

  ngOnInit() {
    const userId = this.auth.user()?.userId;
    if (userId) {
      this.teacherService.loadAllData(userId);
    }
  }

  campaignTypeSelected = toSignal(
    this.form.get('campaignType')!.valueChanges,
    { initialValue: MatchingCampaignType.PROJECT }
  );

  isProjectType = computed(() => this.campaignTypeSelected() === MatchingCampaignType.PROJECT);

  onTypeChange() {
    this.selectedItemIds.set([]);
  }

  // --- LOGIQUE DES SLIDERS (CORRIGÉE) ---

  updateWeight(field: 'skillsWeight' | 'interestsWeight' | 'workTypeWeight', event: Event) {
    const newVal = +(event.target as HTMLInputElement).value;
    const oldVal = this.form.get(field)?.value || 0;
    const diff = newVal - oldVal;

    const otherFields = (['skillsWeight', 'interestsWeight', 'workTypeWeight'] as const)
      .filter(f => f !== field);

    let remainingDiff = diff;

    const currentValues = {
      f1: this.form.get(otherFields[0])?.value || 0,
      f2: this.form.get(otherFields[1])?.value || 0
    };

    let nextF1 = currentValues.f1 - (diff / 2);
    let nextF2 = currentValues.f2 - (diff / 2);

    if (nextF1 < 0) {
      nextF2 += nextF1;
      nextF1 = 0;
    }
    if (nextF2 < 0) {
      nextF1 += nextF2;
      nextF2 = 0;
    }

    if (nextF1 > 100) nextF1 = 100;
    if (nextF2 > 100) nextF2 = 100;

    this.form.patchValue({
      [field]: newVal,
      [otherFields[0]]: Math.round(nextF1),
      [otherFields[1]]: Math.round(nextF2)
    }, { emitEvent: false });

    const finalTotal = (this.form.get('skillsWeight')?.value || 0) +
      (this.form.get('interestsWeight')?.value || 0) +
      (this.form.get('workTypeWeight')?.value || 0);

    if (finalTotal !== 100) {
      const adjustment = 100 - finalTotal;
      const currentF2 = this.form.get(otherFields[1])?.value || 0;
      this.form.get(otherFields[1])?.setValue(currentF2 + adjustment, { emitEvent: false });
    }
  }

  // --- GESTION DES SÉLECTIONS ---

  toggleStudent(id: number) {
    this.selectedStudentIds.update(ids =>
      ids.includes(id) ? ids.filter(i => i !== id) : [...ids, id]
    );
  }

  toggleItem(id: number) {
    this.selectedItemIds.update(ids =>
      ids.includes(id) ? ids.filter(i => i !== id) : [...ids, id]
    );
  }

  selectAllStudents() {
    const allIds = this.availableStudents().map(s => s.id);
    this.selectedStudentIds.set(this.selectedStudentIds().length === allIds.length ? [] : allIds);
  }

  onSubmit() {
    if (this.form.invalid || this.selectedStudentIds().length === 0 || this.selectedItemIds().length === 0) {
      alert("Formulaire incomplet : vérifiez les poids (100%) et les sélections.");
      return;
    }

    this.isSubmitting.set(true);
    const userId = this.auth.user()?.userId!;

    const raw = this.form.getRawValue();
    const payload = {
      ...raw,
      teacherId: userId,
      skillsWeight: (raw.skillsWeight || 0) / 100,
      interestsWeight: (raw.interestsWeight || 0) / 100,
      workTypeWeight: (raw.workTypeWeight || 0) / 100
    };

    this.campaignService.create(payload as any).pipe(
      switchMap(campaign => forkJoin({
        st: this.campaignService.addStudents(campaign.id, this.selectedStudentIds()),
        it: this.campaignService.addItems(campaign.id, this.selectedItemIds())
      })),
      finalize(() => this.isSubmitting.set(false))
    ).subscribe({
      next: () => this.router.navigate(['/app/teacher/dashboard']),
      error: (err) => console.error(err)
    });
  }
}
