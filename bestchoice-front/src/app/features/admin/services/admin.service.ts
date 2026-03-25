import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Role } from '../../../core/models/enums.model';

export interface UserResponse {
  id: number;
  studentNumber: string | null;
  firstName: string;
  lastName: string;
  email: string;
  active: boolean;
  role: Role;
  createdAt: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  studentNumber?: string | null;
  role: Role;
}

export interface RegisterResponse {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  message: string;
}

export interface NameCountEntry { name: string; count: number; }

export interface AdminStats {
  totalProjects: number;
  activeProjects: number;
  inactiveProjects: number;
  completedProjects: number;
  totalProjectCapacity: number;
  projectsByWorkType: Record<string, number>;
  projectsBySemester: Record<string, number>;
  topTeachersByProjects: NameCountEntry[];

  totalSubjects: number;
  activeSubjects: number;
  inactiveSubjects: number;
  totalSubjectCapacity: number;
  subjectsByWorkType: Record<string, number>;
  subjectsBySemester: Record<string, number>;
  topTeachersBySubjects: NameCountEntry[];

  totalTeachers: number;
  totalStudents: number;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly API = `${environment.apiBaseUrl}/api/users`;

  getStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${environment.apiBaseUrl}/api/admin/stats`);
  }

  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(this.API);
  }

  createUser(req: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.API}/register`, req);
  }

  updateUser(id: number, req: { firstName: string; lastName: string; email: string; studentNumber?: string | null }): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.API}/${id}`, req);
  }

  deactivateUser(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/${id}/deactivate`, {});
  }

  activateUser(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/${id}/activate`, {});
  }
}
