import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/api/auth`;

  login(payload: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.base}/login`, payload);
  }

  register(payload: RegisterRequest) {
    return this.http.post<RegisterResponse>(`${this.base}/register`, payload);
  }

  forgotPassword(email: string) {
    return this.http.post<void>(`${this.base}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string, confirmNewPassword: string) {
    return this.http.post<void>(`${this.base}/reset-password`, {
      token,
      newPassword,
      confirmNewPassword
    });
  }
}
