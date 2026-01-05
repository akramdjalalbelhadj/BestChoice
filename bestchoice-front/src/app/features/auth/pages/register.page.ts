import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, NonNullableFormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth.api';
import { Role } from '../../../core/models/auth.model';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page">
      <div class="card">
        <h1>Créer un compte</h1>

        <p *ngIf="success()" class="success">{{ success() }}</p>
        <p *ngIf="error()" class="error">{{ error() }}</p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="grid">
            <div>
              <label>Prénom</label>
              <input formControlName="firstName" />
            </div>
            <div>
              <label>Nom</label>
              <input formControlName="lastName" />
            </div>
          </div>

          <label>Email</label>
          <input type="email" formControlName="email" />

          <label>Mot de passe</label>
          <input type="password" formControlName="password" />

          <label>Numéro étudiant (optionnel)</label>
          <input formControlName="studentNumber" />

          <label>Rôle</label>
          <select formControlName="role">
            <option value="ADMIN">ADMIN</option>
            <option value="ETUDIANT">ETUDIANT</option>
            <option value="ENSEIGNANT">ENSEIGNANT</option>
          </select>

          <button class="btn" [disabled]="form.invalid || loading()">Créer</button>
        </form>

        <div class="links">
          <a routerLink="/auth/login">Retour login</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page{min-height:100vh;display:grid;place-items:center;padding:24px}
    .card{width:100%;max-width:520px;border:1px solid rgba(255,255,255,.12);border-radius:16px;padding:24px;background:rgba(255,255,255,.04)}
    .muted{opacity:.75;margin:0 0 16px}
    form{display:grid;gap:10px}
    .grid{display:grid;grid-template-columns:1fr 1fr;gap:10px}
    label{font-size:13px;opacity:.8}
    input, select{padding:12px 12px;border-radius:12px;border:1px solid rgba(255,255,255,.12);background:rgba(0,0,0,.15);color:inherit;outline:none}
    .btn{margin-top:6px;padding:12px 14px;border-radius:12px;border:0;background:#ffffff;color:#000;font-weight:600;cursor:pointer}
    .btn:disabled{opacity:.5;cursor:not-allowed}
    .error{color:#ff8a8a}
    .success{color:#8affb1}
    .links{margin-top:14px;font-size:13px}
  `]
})
export class RegisterPage {
  private fb = inject(NonNullableFormBuilder);
  private api = inject(AuthApi);

  loading = signal(false);
  error = signal<string | null>(null);
  success = signal<string | null>(null);

  form = this.fb.group({
    firstName: this.fb.control('', [Validators.required, Validators.minLength(2)]),
    lastName: this.fb.control('', [Validators.required, Validators.minLength(2)]),
    email: this.fb.control('', [Validators.required, Validators.email]),
    password: this.fb.control('', [Validators.required, Validators.minLength(8)]),
    studentNumber: this.fb.control(''),
    role: this.fb.control<Role>('ADMIN', [Validators.required])
  });

  submit() {
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);

    const v = this.form.getRawValue();

    this.api.register({
      firstName: v.firstName,
      lastName: v.lastName,
      email: v.email,
      password: v.password,
      studentNumber: v.studentNumber.trim() || null,
      role: v.role
    }).subscribe({
      next: (res) => this.success.set(res.message ?? 'Compte créé.'),
      error: (err) => this.error.set(err?.error?.message ?? 'Inscription impossible.'),
      complete: () => this.loading.set(false)
    });
  }
}
