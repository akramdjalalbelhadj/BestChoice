import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, NonNullableFormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth.api';
import { AuthStore } from '../../../core/auth/auth.store';
import { finalize } from 'rxjs';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="page">
      <div class="card">
        <h1>Connexion</h1>
        <p class="muted">Connectez-vous pour accéder à votre espace.</p>

        @if (error()) {
          <p class="error" role="alert">{{ error() }}</p>
        }

        <form [formGroup]="form" (ngSubmit)="submit()">
          <label for="email">Email</label>
          <input
            id="email"
            type="email"
            formControlName="email"
            placeholder="ex: admin@bestchoice.local" />

          <label for="password">Mot de passe</label>
          <input
            id="password"
            type="password"
            formControlName="password"
            placeholder="********" />

          <button class="btn" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Chargement...' : 'Se connecter' }}
          </button>
        </form>

        <div class="links">
          <a routerLink="/auth/forgot-password">Mot de passe oublié ?</a>
          <span>•</span>
          <a routerLink="/auth/register">Créer un compte</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; }
    .page { min-height: 100vh; display: grid; place-items: center; padding: 24px; background: #000; color: #fff; }
    .card { width: 100%; max-width: 420px; border: 1px solid rgba(255,255,255,.12); border-radius: 16px; padding: 24px; background: rgba(255,255,255,.04); }
    .muted { opacity: .75; margin: 0 0 16px; font-size: 14px; }
    form { display: grid; gap: 12px; }
    label { font-size: 13px; opacity: .8; font-weight: 500; }
    input {
      padding: 12px; border-radius: 12px; border: 1px solid rgba(255,255,255,.12);
      background: rgba(0,0,0,.2); color: inherit; outline: none; transition: border-color 0.2s;
    }
    input:focus { border-color: rgba(255,255,255,.5); }
    .btn {
      margin-top: 8px; padding: 14px; border-radius: 12px; border: 0;
      background: #fff; color: #000; font-weight: 600; cursor: pointer; transition: opacity 0.2s;
    }
    .btn:disabled { opacity: .5; cursor: not-allowed; }
    .error { color: #ff8a8a; background: rgba(255, 138, 138, 0.1); padding: 10px; border-radius: 8px; font-size: 14px; }
    .links { margin-top: 20px; font-size: 13px; display: flex; gap: 10px; align-items: center; justify-content: center; }
    .links a { color: inherit; text-decoration: none; opacity: 0.8; }
    .links a:hover { opacity: 1; text-decoration: underline; }
  `]
})
export class LoginPage {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(AuthApi);
  private readonly auth = inject(AuthStore);
  private readonly router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  submit() {
    if (this.form.invalid || this.loading()) return;

    this.loading.set(true);
    this.error.set(null);

    this.api.login(this.form.getRawValue())
        .pipe(finalize(() => this.loading.set(false)))
        .subscribe({
          next: (res) => {
            this.auth.setFromLogin(res);
            this.router.navigateByUrl('/app');
          },
          error: (err) => this.error.set(err?.error?.message ?? 'Connexion impossible.')
        });
  }
}
