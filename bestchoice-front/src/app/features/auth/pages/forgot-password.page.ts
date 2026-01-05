import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth.api';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
  <div class="page">
    <div class="card">
      <h1>Mot de passe oublié</h1>
      <p class="muted">On t’enverra un email si le compte existe.</p>

      <p *ngIf="success()" class="success">{{ success() }}</p>
      <p *ngIf="error()" class="error">{{ error() }}</p>

      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Email</label>
        <input type="email" formControlName="email" placeholder="ex: prenom.nom@etu.univ-amu.fr" />
        <button class="btn" [disabled]="form.invalid || loading()">Envoyer</button>
      </form>

      <div class="links">
        <a routerLink="/auth/login">Retour login</a>
      </div>
    </div>
  </div>
  `,
  styles: [`
    .page{min-height:100vh;display:grid;place-items:center;padding:24px}
    .card{width:100%;max-width:420px;border:1px solid rgba(255,255,255,.12);border-radius:16px;padding:24px;background:rgba(255,255,255,.04)}
    .muted{opacity:.75;margin:0 0 16px}
    form{display:grid;gap:10px}
    label{font-size:13px;opacity:.8}
    input{padding:12px 12px;border-radius:12px;border:1px solid rgba(255,255,255,.12);background:rgba(0,0,0,.15);color:inherit;outline:none}
    .btn{margin-top:6px;padding:12px 14px;border-radius:12px;border:0;background:#ffffff;color:#000;font-weight:600;cursor:pointer}
    .btn:disabled{opacity:.5;cursor:not-allowed}
    .error{color:#ff8a8a}
    .success{color:#8affb1}
  `]
})
export class ForgotPasswordPage {
  private fb = inject(FormBuilder);
  private api = inject(AuthApi);

  loading = signal(false);
  error = signal<string | null>(null);
  success = signal<string | null>(null);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  submit() {
    if (this.form.invalid || this.loading()) return;
    this.loading.set(true);
    this.error.set(null);
    this.success.set(null);

    this.api.forgotPassword(this.form.value.email!).subscribe({
      next: () => this.success.set('Si le compte existe, un email a été envoyé.'),
      error: (err) => this.error.set(err?.error?.message ?? 'Action impossible.'),
      complete: () => this.loading.set(false)
    });
  }
}
