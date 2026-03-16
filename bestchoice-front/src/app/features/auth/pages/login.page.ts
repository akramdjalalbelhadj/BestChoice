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

    .page {
      min-height: 100vh;
      display: grid;
      place-items: center;
      padding: 24px;
      background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
    }

    .card {
      width: 100%;
      max-width: 460px;
      border: 1px solid #e9ecef;
      border-radius: 20px;
      padding: 3rem;
      background: #ffffff;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }

    h1 {
      font-size: 2rem;
      font-weight: 700;
      margin: 0 0 0.5rem 0;
      color: #212529;
      letter-spacing: -0.02em;
    }

    .muted {
      color: #6c757d;
      margin: 0 0 2rem 0;
      font-size: 1rem;
      line-height: 1.5;
    }

    form {
      display: grid;
      gap: 1.5rem;
    }

    label {
      font-size: 0.9rem;
      font-weight: 600;
      color: #212529;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }

    input {
      padding: 12px 16px;
      border-radius: 10px;
      border: 1.5px solid #e9ecef;
      background: #f8f9fa;
      color: #212529;
      outline: none;
      font-size: 0.95rem;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
    }

    input::placeholder {
      color: #adb5bd;
    }

    input:focus {
      border-color: #007bff;
      background: #ffffff;
      box-shadow: 0 0 0 4px rgba(0, 123, 255, 0.1), 0 2px 8px rgba(0, 0, 0, 0.05);
    }

    input:hover:not(:focus) {
      border-color: #d1d5db;
      background: #ffffff;
    }

    .btn {
      margin-top: 0.5rem;
      padding: 12px 24px;
      border-radius: 10px;
      border: none;
      background: #007bff;
      color: #ffffff;
      font-weight: 700;
      font-size: 0.95rem;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 4px 12px rgba(0, 123, 255, 0.2);
    }

    .btn:hover:not(:disabled) {
      background: #0056cc;
      box-shadow: 0 6px 16px rgba(0, 123, 255, 0.3);
      transform: translateY(-2px);
    }

    .btn:active:not(:disabled) {
      transform: translateY(0);
      box-shadow: 0 2px 4px rgba(0, 123, 255, 0.15);
    }

    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .btn:focus {
      outline: none;
      box-shadow: 0 0 0 4px rgba(0, 123, 255, 0.15);
    }

    .error {
      color: #dc3545;
      background: rgba(220, 53, 69, 0.08);
      border: 1px solid rgba(220, 53, 69, 0.25);
      padding: 12px 16px;
      border-radius: 10px;
      font-size: 0.9rem;
      font-weight: 500;
      margin-bottom: 1rem;
    }

    .links {
      margin-top: 2rem;
      font-size: 0.9rem;
      display: flex;
      gap: 12px;
      align-items: center;
      justify-content: center;
      flex-wrap: wrap;
    }

    .links a {
      color: #007bff;
      text-decoration: none;
      font-weight: 600;
      transition: all 0.3s ease;
      padding: 4px 8px;
      border-radius: 6px;
    }

    .links a:hover {
      color: #0056cc;
      background: rgba(0, 123, 255, 0.08);
    }

    .links span {
      color: #dee2e6;
    }
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
