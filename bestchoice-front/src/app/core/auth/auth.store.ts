import { Injectable, computed, signal } from '@angular/core';
import { LoginResponse } from '../models/auth.model';

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
  isAuthenticated = computed(() => {
    const s = this.state();
    return !!s.token && !!s.expiresAt && Date.now() < s.expiresAt;
  });

  setFromLogin(res: LoginResponse) {
    const expiresAt = Date.now() + res.expiresIn;
    const user = {
      userId: res.userId,
      email: res.email,
      firstName: res.firstName,
      lastName: res.lastName,
      roles: res.roles
    };
    const next: AuthState = { user, token: res.accessToken, expiresAt };
    this.state.set(next);
    saveState(next);
  }

  logout() {
    const next: AuthState = { user: null, token: null, expiresAt: null };
    this.state.set(next);
    localStorage.removeItem(LS_KEY);
  }
}
