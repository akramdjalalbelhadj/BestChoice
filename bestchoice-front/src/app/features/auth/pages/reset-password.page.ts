import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthApi } from '../../../core/api/auth.api';

@Component({
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
  <div class="page">
    <div class="card">
      <h1>Nouveau mot de passe</h1>

      @if (!token()) {
        <div class="error-box">
          ⚠️ Lien invalide. Veuillez refaire une demande de réinitialisation.
          <br><br>
          <a routerLink="/auth/forgot-password" class="link">Mot de passe oublié</a>
        </div>
      } @else if (done()) {
        <div class="success-box">
          ✅ Mot de passe modifié avec succès !
          <br><br>
          <a routerLink="/auth/login" class="link">Se connecter</a>
        </div>
      } @else {
        <p class="muted">Choisissez un nouveau mot de passe.</p>

        @if (error()) {
          <div class="error-box">⚠️ {{ error() }}</div>
        }

        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>Nouveau mot de passe</label>
          <input type="password" formControlName="newPassword"
                 placeholder="Minimum 8 caractères" />

          <label>Confirmer le mot de passe</label>
          <input type="password" formControlName="confirmNewPassword"
                 placeholder="Répétez le mot de passe" />

          @if (form.errors?.['mismatch'] && form.get('confirmNewPassword')?.dirty) {
            <p class="field-error">Les mots de passe ne correspondent pas.</p>
          }

          <button class="btn" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Enregistrement...' : 'Enregistrer' }}
          </button>
        </form>
      }
    </div>
  </div>
  `,
  styles: [`
    .page { min-height: 100vh; display: flex; align-items: center; justify-content: center;
            background: var(--bc-bg); }
    .card { background: var(--bc-card); border: 1px solid var(--bc-border); border-radius: 16px;
            padding: 2.5rem; width: 100%; max-width: 420px;
            box-shadow: 0 8px 32px rgba(0,0,0,.08); }
    h1 { font-size: 1.5rem; font-weight: 800; color: var(--bc-text); margin-bottom: 0.25rem; }
    .muted { font-size: 0.85rem; color: var(--bc-text-2); margin-bottom: 1.5rem; }
    label { display: block; font-size: 0.78rem; font-weight: 700; color: var(--bc-text-2);
            text-transform: uppercase; letter-spacing: .05em; margin: 1rem 0 0.3rem; }
    input { width: 100%; padding: 0.65rem 0.9rem; border: 1.5px solid var(--bc-border);
            border-radius: 10px; background: var(--bc-bg); color: var(--bc-text);
            font-size: 0.9rem; box-sizing: border-box; }
    input:focus { outline: none; border-color: var(--bc-primary); }
    .btn { margin-top: 1.5rem; width: 100%; padding: 0.75rem; border: none;
           border-radius: 10px; background: var(--bc-primary); color: white;
           font-weight: 700; font-size: 0.92rem; cursor: pointer; transition: opacity .2s; }
    .btn:disabled { opacity: .6; cursor: not-allowed; }
    .error-box { background: #fee2e2; color: #991b1b; padding: 0.75rem 1rem;
                 border-radius: 10px; font-size: 0.85rem; margin-bottom: 1rem; }
    .success-box { background: #d1fae5; color: #065f46; padding: 0.75rem 1rem;
                   border-radius: 10px; font-size: 0.85rem; }
    .field-error { color: #dc2626; font-size: 0.78rem; margin-top: 0.3rem; }
    .link { color: var(--bc-primary); font-weight: 600; text-decoration: none; }
  `]
})
export class ResetPasswordPage implements OnInit {
  private fb     = inject(FormBuilder);
  private api    = inject(AuthApi);
  private route  = inject(ActivatedRoute);
  private router = inject(Router);

  token   = signal<string | null>(null);
  loading = signal(false);
  error   = signal<string | null>(null);
  done    = signal(false);

  form = this.fb.group({
    newPassword:        ['', [Validators.required, Validators.minLength(8)]],
    confirmNewPassword: ['', Validators.required]
  }, { validators: this.passwordMatchValidator });

  ngOnInit() {
    const t = this.route.snapshot.queryParamMap.get('token');
    this.token.set(t);
  }

  submit() {
    if (this.form.invalid || this.loading() || !this.token()) return;
    this.loading.set(true);
    this.error.set(null);

    const { newPassword, confirmNewPassword } = this.form.value;
    this.api.resetPassword(this.token()!, newPassword!, confirmNewPassword!).subscribe({
      next: () => { this.done.set(true); this.loading.set(false); },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Une erreur est survenue.');
        this.loading.set(false);
      }
    });
  }

  private passwordMatchValidator(group: AbstractControl) {
    const pwd  = group.get('newPassword')?.value;
    const conf = group.get('confirmNewPassword')?.value;
    return pwd && conf && pwd !== conf ? { mismatch: true } : null;
  }
}
