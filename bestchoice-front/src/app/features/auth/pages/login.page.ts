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
  styleUrl: './login.page.scss'
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
