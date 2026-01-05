import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth.api';
import { AuthStore } from '../../../core/auth/auth.store';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
  <div class="page">
    <div class="card">
      <h1>Connexion</h1>
      <p class="muted">Connecte-toi avec ton email et ton mot de passe.</p>

      <p *ngIf="error()" class="error">{{ error() }}</p>

      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Email</label>
        <input type="email" formControlName="email" placeholder="ex: admin@bestchoice.local" />

        <label>Mot de passe</label>
        <input type="password" formControlName="password" placeholder="••••••••" />

        <button class="btn" [disabled]="form.invalid || loading()">Se connecter</button>
      </form>

      <div class="links">
        <a routerLink="/auth/forgot-password">Mot de passe oublié ?</a>
        <a routerLink="/auth/register">Créer un compte</a>
      </div>
    </div>
  </div>
  `,
  styles: [`
    .page{min-height:100vh;display:grid;place-items:center;padding:24px}
    .card{width:100%;max-width:420px;border:1px solid rgba(255,255,255,.12);border-radius:16px;padding:24px;background:rgba(255,255,255,.04)}
    h1{margin:0 0 8px}
    .muted{opacity:.75;margin:0 0 16px}
    form{display:grid;gap:10px}
    label{font-size:13px;opacity:.8}
    input{padding:12px 12px;border-radius:12px;border:1px solid rgba(255,255,255,.12);background:rgba(0,0,0,.15);color:inherit;outline:none}
    .btn{margin-top:6px;padding:12px 14px;border-radius:12px;border:0;background:#ffffff;color:#000;font-weight:600;cursor:pointer}
    .btn:disabled{opacity:.5;cursor:not-allowed}
    .error{color:#ff8a8a;margin:8px 0}
    .links{display:flex;justify-content:space-between;margin-top:14px;font-size:13px}
    a{opacity:.85}
  `]
})
export class LoginPage {
  private fb = inject(FormBuilder);
  private api = inject(AuthApi);
  private store = inject(AuthStore);
  private router = inject(Router);

  loading = signal(false);
  error = signal<string | null>(null);

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  submit() {
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    this.error.set(null);

    this.api.login({
      email: this.form.value.email!,
      password: this.form.value.password!
    }).subscribe({
      next: (res) => {
        this.store.setFromLogin(res);
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Connexion impossible.';
        this.error.set(msg);
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }
}
