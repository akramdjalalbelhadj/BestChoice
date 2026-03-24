import { Injectable, computed, signal } from '@angular/core';
import { LoginResponse, Role } from '../models/auth.model';
import { isJwtExpired } from '../utils/jwt.util';

const LS_KEY = 'bestchoice_auth';

type AuthState = {
  user: Omit<LoginResponse, 'accessToken' | 'tokenType' | 'expiresIn'> | null;
  token: string | null;
  expiresAt: number | null;
};

function loadState(): AuthState {
  const raw = localStorage.getItem(LS_KEY);
  if (!raw) return { user: null, token: null, expiresAt: null };
  try {
    return JSON.parse(raw) as AuthState;
  } catch {
    return { user: null, token: null, expiresAt: null };
  }
}

function saveState(s: AuthState) {
  localStorage.setItem(LS_KEY, JSON.stringify(s));
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private state = signal<AuthState>(loadState());

  user = computed(() => this.state().user);
  token = computed(() => this.state().token);

  role = computed<Role | null>(() => this.state().user?.role ?? null);

  displayName = computed(() => {
    const u = this.state().user;
    return u ? `${u.firstName} ${u.lastName}` : null;
  });

  isAuthenticated = computed(() => {
    const s = this.state();
    if (!s.token || !s.expiresAt) return false;
    if (Date.now() >= s.expiresAt) return false;
    if (isJwtExpired(s.token)) return false;
    return true;
  });

  homeUrlForRole(role: Role | null): string {
    switch (role) {
      case 'ADMIN':
        return '/app/admin';
      case 'ENSEIGNANT':
        return '/app/teacher';
      case 'ETUDIANT':
        return '/app/student';
      default:
        return '/dashboard';
    }
  }

  homeUrl = computed(() => this.homeUrlForRole(this.role()));

  setFromLogin(res: LoginResponse) {
    const expiresAt = Date.now() + res.expiresIn;
    const user = {
      userId: res.userId,
      email: res.email,
      firstName: res.firstName,
      lastName: res.lastName,
      role: res.role
    };
    const next: AuthState = { user, token: res.accessToken, expiresAt };
    this.state.set(next);
    saveState(next);
  }

  logout() {
    this.state.set({ user: null, token: null, expiresAt: null });
    localStorage.removeItem(LS_KEY);
  }
}
