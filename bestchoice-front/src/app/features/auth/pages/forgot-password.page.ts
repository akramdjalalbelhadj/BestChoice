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
  styleUrl: './forgot-password.page.scss'
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
