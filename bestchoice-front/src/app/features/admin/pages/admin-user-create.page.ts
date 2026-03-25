import {
  Component, inject, signal, computed,
  ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AdminService } from '../services/admin.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../shared/theme-toggle.component';
import { Role } from '../../../core/models/enums.model';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-user-create.page.html',
  styleUrl: './admin-user-create.page.scss'
})
export class AdminUserCreatePage {
  private readonly adminService = inject(AdminService);
  protected readonly auth       = inject(AuthStore);
  private readonly router       = inject(Router);
  private readonly fb           = inject(FormBuilder);
  private readonly cdr          = inject(ChangeDetectorRef);

  readonly Role = Role;

  isSubmitting = signal(false);
  errorMsg     = signal<string | null>(null);

  // ── Signaux d'erreur ──────────────────────────────────────────────────────
  firstNameError  = signal(false);
  lastNameError   = signal(false);
  emailError      = signal(false);
  passwordError   = signal(false);
  studentNumError = signal(false);

  form = this.fb.group({
    firstName:     ['', [Validators.required, Validators.minLength(2)]],
    lastName:      ['', [Validators.required, Validators.minLength(2)]],
    email:         ['', [Validators.required, Validators.email]],
    password:      ['', [Validators.required, Validators.minLength(8)]],
    role:          [Role.TEACHER as Role, [Validators.required]],
    studentNumber: ['']
  });

  isStudentRole = signal(false);

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  constructor() {
    // Effacer les erreurs dès que l'utilisateur corrige
    this.form.get('firstName')!.valueChanges.subscribe(v => {
      if ((v?.trim().length ?? 0) >= 2) { this.firstNameError.set(false); this.cdr.markForCheck(); }
    });
    this.form.get('lastName')!.valueChanges.subscribe(v => {
      if ((v?.trim().length ?? 0) >= 2) { this.lastNameError.set(false); this.cdr.markForCheck(); }
    });
    this.form.get('email')!.valueChanges.subscribe(v => {
      if (v?.includes('@')) { this.emailError.set(false); this.cdr.markForCheck(); }
    });
    this.form.get('password')!.valueChanges.subscribe(v => {
      if ((v?.length ?? 0) >= 8) { this.passwordError.set(false); this.cdr.markForCheck(); }
    });
    this.form.get('studentNumber')!.valueChanges.subscribe(() => {
      this.studentNumError.set(false); this.cdr.markForCheck();
    });
  }

  onRoleChange(role: Role) {
    this.form.get('role')?.setValue(role);
    this.isStudentRole.set(role === Role.STUDENT);
    if (role !== Role.STUDENT) {
      this.form.get('studentNumber')?.setValue('');
      this.studentNumError.set(false);
    }
    this.cdr.markForCheck();
  }

  onSubmit() {
    const raw = this.form.getRawValue();

    const firstNameOk  = (raw.firstName?.trim().length ?? 0) >= 2;
    const lastNameOk   = (raw.lastName?.trim().length ?? 0) >= 2;
    const emailOk      = !!raw.email?.trim() && raw.email.includes('@') && raw.email.includes('.');
    const passwordOk   = (raw.password?.length ?? 0) >= 8;
    const studentNumOk = raw.role !== Role.STUDENT || !!(raw.studentNumber?.trim());

    this.firstNameError.set(!firstNameOk);
    this.lastNameError.set(!lastNameOk);
    this.emailError.set(!emailOk);
    this.passwordError.set(!passwordOk);
    this.studentNumError.set(!studentNumOk);
    this.cdr.markForCheck();

    if (!firstNameOk || !lastNameOk || !emailOk || !passwordOk || !studentNumOk) return;

    this.isSubmitting.set(true);
    this.errorMsg.set(null);

    const payload = {
      firstName:     raw.firstName!.trim(),
      lastName:      raw.lastName!.trim(),
      email:         raw.email!.trim(),
      password:      raw.password!,
      role:          raw.role as Role,
      studentNumber: raw.role === Role.STUDENT ? (raw.studentNumber?.trim() || null) : null
    };

    this.adminService.createUser(payload)
      .pipe(finalize(() => { this.isSubmitting.set(false); this.cdr.markForCheck(); }))
      .subscribe({
        next: () => this.router.navigate(['/app/admin/users']),
        error: err => {
          this.errorMsg.set(err?.error?.message ?? 'Une erreur est survenue lors de la création.');
          this.cdr.markForCheck();
        }
      });
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
