import {
  Component, inject, OnInit, signal, computed,
  ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AdminService, UserResponse } from '../services/admin.service';
import { AuthStore } from '../../../core/auth/auth.store';
import { ThemeToggleComponent } from '../../../shared/theme-toggle.component';
import { Role } from '../../../core/models/enums.model';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink, ThemeToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-users.page.html',
  styleUrl: './admin-users.page.scss'
})
export class AdminUsersPage implements OnInit {
  private readonly adminService = inject(AdminService);
  protected readonly auth        = inject(AuthStore);
  private readonly router        = inject(Router);
  private readonly fb            = inject(FormBuilder);
  private readonly cdr           = inject(ChangeDetectorRef);

  // ── État ──────────────────────────────────────────────────────────────────
  users          = signal<UserResponse[]>([]);
  isLoading      = signal(true);
  showForm       = signal(false);
  isSubmitting   = signal(false);
  successMsg     = signal<string | null>(null);
  errorMsg       = signal<string | null>(null);

  // ── Édition ───────────────────────────────────────────────────────────────
  editingUser      = signal<UserResponse | null>(null);
  isEditSubmitting = signal(false);
  editFirstNameError = signal(false);
  editLastNameError  = signal(false);
  editEmailError     = signal(false);

  // ── Filtres & Pagination ──────────────────────────────────────────────────
  searchQuery  = signal('');
  roleFilter   = signal<'ALL' | 'ENSEIGNANT' | 'ETUDIANT'>('ALL');
  currentPage  = signal(1);
  readonly pageSize = 5;

  // ── Erreurs de formulaire ────────────────────────────────────────────────
  firstNameError    = signal(false);
  lastNameError     = signal(false);
  emailError        = signal(false);
  passwordError     = signal(false);
  studentNumError   = signal(false);

  readonly Role = Role;

  // ── Formulaire ────────────────────────────────────────────────────────────
  form = this.fb.group({
    firstName:     ['', [Validators.required, Validators.minLength(2)]],
    lastName:      ['', [Validators.required, Validators.minLength(2)]],
    email:         ['', [Validators.required, Validators.email]],
    password:      ['', [Validators.required, Validators.minLength(8)]],
    role:          [Role.TEACHER as Role, [Validators.required]],
    studentNumber: ['']
  });

  isStudentRole = computed(() => this.form.get('role')?.value === Role.STUDENT);

  editForm = this.fb.group({
    firstName:     ['', [Validators.required, Validators.minLength(2)]],
    lastName:      ['', [Validators.required, Validators.minLength(2)]],
    email:         ['', [Validators.required, Validators.email]],
    studentNumber: ['']
  });

  initials = computed(() => {
    const name = this.auth.displayName();
    return name ? name.split(' ').map(n => n[0]).join('').toUpperCase() : '?';
  });

  // ── Données filtrées (exclut les ADMIN) ──────────────────────────────────
  filteredUsers = computed(() => {
    const q    = this.searchQuery().toLowerCase().trim();
    const role = this.roleFilter();
    return this.users().filter(u => {
      if (u.role !== Role.TEACHER && u.role !== Role.STUDENT) return false;
      const matchRole = role === 'ALL' || u.role === role;
      const matchQ    = !q ||
        u.firstName.toLowerCase().includes(q) ||
        u.lastName.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        (u.studentNumber ?? '').toLowerCase().includes(q);
      return matchRole && matchQ;
    });
  });

  totalPages = computed(() => Math.max(1, Math.ceil(this.filteredUsers().length / this.pageSize)));

  paginatedUsers = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredUsers().slice(start, start + this.pageSize);
  });

  pageNumbers = computed(() => {
    const total = this.totalPages();
    const current = this.currentPage();
    const maxVisible = 5;
    let start = Math.max(1, current - Math.floor(maxVisible / 2));
    let end   = Math.min(total, start + maxVisible - 1);
    if (end - start + 1 < maxVisible) start = Math.max(1, end - maxVisible + 1);
    const pages: number[] = [];
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  });

  paginationInfo = computed(() => {
    const total = this.filteredUsers().length;
    if (total === 0) return '';
    const start = (this.currentPage() - 1) * this.pageSize + 1;
    const end   = Math.min(this.currentPage() * this.pageSize, total);
    return `${start}–${end} sur ${total}`;
  });

  teacherCount = computed(() => this.users().filter(u => u.role === Role.TEACHER).length);
  studentCount = computed(() => this.users().filter(u => u.role === Role.STUDENT).length);

  ngOnInit() {
    this.loadUsers();

    // Effacer erreurs en temps réel
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

  private loadUsers() {
    this.isLoading.set(true);
    this.adminService.getAllUsers()
      .pipe(finalize(() => { this.isLoading.set(false); this.cdr.markForCheck(); }))
      .subscribe({
        next: users => { this.users.set(users); this.cdr.markForCheck(); },
        error: () => { this.errorMsg.set('Impossible de charger les utilisateurs.'); this.cdr.markForCheck(); }
      });
  }

  toggleForm() {
    this.showForm.update(v => !v);
    if (!this.showForm()) {
      this.form.reset({ role: Role.TEACHER });
      this.clearErrors();
      this.errorMsg.set(null);
    }
    this.cdr.markForCheck();
  }

  onRoleChange() {
    if (this.form.get('role')?.value !== Role.STUDENT) {
      this.form.get('studentNumber')?.setValue('');
    }
    this.cdr.markForCheck();
  }

  onSubmit() {
    const raw = this.form.getRawValue();

    const firstNameOk = (raw.firstName?.trim().length ?? 0) >= 2;
    const lastNameOk  = (raw.lastName?.trim().length ?? 0) >= 2;
    const emailOk     = !!raw.email?.includes('@');
    const passwordOk  = (raw.password?.length ?? 0) >= 8;
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
    this.successMsg.set(null);

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
        next: res => {
          this.successMsg.set(`✅ ${res.firstName} ${res.lastName} créé avec succès.`);
          this.form.reset({ role: Role.TEACHER });
          this.clearErrors();
          this.showForm.set(false);
          this.loadUsers();
          this.cdr.markForCheck();
        },
        error: err => {
          this.errorMsg.set(err?.error?.message ?? 'Une erreur est survenue lors de la création.');
          this.cdr.markForCheck();
        }
      });
  }

  startEdit(user: UserResponse) {
    this.editingUser.set(user);
    this.editForm.patchValue({
      firstName:     user.firstName,
      lastName:      user.lastName,
      email:         user.email,
      studentNumber: user.studentNumber ?? ''
    });
    this.editFirstNameError.set(false);
    this.editLastNameError.set(false);
    this.editEmailError.set(false);
    this.errorMsg.set(null);
    this.cdr.markForCheck();
  }

  cancelEdit() {
    this.editingUser.set(null);
    this.editForm.reset();
    this.cdr.markForCheck();
  }

  saveEdit() {
    const raw = this.editForm.getRawValue();
    const firstNameOk = (raw.firstName?.trim().length ?? 0) >= 2;
    const lastNameOk  = (raw.lastName?.trim().length ?? 0) >= 2;
    const emailOk     = !!raw.email?.includes('@') && !!raw.email?.includes('.');

    this.editFirstNameError.set(!firstNameOk);
    this.editLastNameError.set(!lastNameOk);
    this.editEmailError.set(!emailOk);
    this.cdr.markForCheck();

    if (!firstNameOk || !lastNameOk || !emailOk) return;

    const user = this.editingUser()!;
    this.isEditSubmitting.set(true);
    this.errorMsg.set(null);

    const payload = {
      firstName:     raw.firstName!.trim(),
      lastName:      raw.lastName!.trim(),
      email:         raw.email!.trim(),
      studentNumber: user.role === Role.STUDENT ? (raw.studentNumber?.trim() || null) : null
    };

    this.adminService.updateUser(user.id, payload)
      .pipe(finalize(() => { this.isEditSubmitting.set(false); this.cdr.markForCheck(); }))
      .subscribe({
        next: updated => {
          this.users.update(all => all.map(u => u.id === user.id ? { ...u, ...updated } : u));
          this.successMsg.set(`✅ ${updated.firstName} ${updated.lastName} mis à jour avec succès.`);
          this.editingUser.set(null);
          this.cdr.markForCheck();
        },
        error: err => {
          this.errorMsg.set(err?.error?.message ?? 'Erreur lors de la mise à jour.');
          this.cdr.markForCheck();
        }
      });
  }

  toggleUserStatus(user: UserResponse) {
    const action$ = user.active
      ? this.adminService.deactivateUser(user.id)
      : this.adminService.activateUser(user.id);
    action$.subscribe({
      next: () => {
        this.users.update(all => all.map(u => u.id === user.id ? { ...u, active: !user.active } : u));
        this.cdr.markForCheck();
      }
    });
  }

  setFilter(f: 'ALL' | 'ENSEIGNANT' | 'ETUDIANT') {
    this.roleFilter.set(f);
    this.currentPage.set(1);
    this.cdr.markForCheck();
  }

  onSearch(q: string) {
    this.searchQuery.set(q);
    this.currentPage.set(1);
    this.cdr.markForCheck();
  }

  prevPage()          { this.currentPage.update(p => Math.max(1, p - 1)); }
  nextPage()          { this.currentPage.update(p => Math.min(this.totalPages(), p + 1)); }
  goToPage(n: number) { this.currentPage.set(n); }

  private clearErrors() {
    this.firstNameError.set(false);
    this.lastNameError.set(false);
    this.emailError.set(false);
    this.passwordError.set(false);
    this.studentNumError.set(false);
  }

  logout() {
    this.auth.logout();
    this.router.navigateByUrl('/auth/login');
  }
}
