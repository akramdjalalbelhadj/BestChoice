import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ProjectResponse, ProjectCreateRequest } from '../../project/models/project.model';
import { tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TeacherService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiBaseUrl}/api/projects`;

  // On garde le signal privé pour la mutation, et public pour la lecture
  private readonly _projects = signal<ProjectResponse[]>([]);
  readonly projects = this._projects.asReadonly();

  /** NOUVEAU : Récupère un projet par son ID */
  getProjectById(id: number) {
    return this.http.get<ProjectResponse>(`${this.API}/${id}`);
  }

  /** Charge uniquement les projets de l'enseignant connecté */
  loadMyProjects(teacherId: number) {
    return this.http.get<ProjectResponse[]>(`${this.API}/teacher/${teacherId}`).pipe(
      tap(mine => this._projects.set(mine))
    );
  }

  activate(id: number) {
    return this.http.patch<void>(`${this.API}/${id}/activate`, {}).pipe(
      tap(() => this.updateLocalStatus(id, true))
    );
  }

  deactivate(id: number) {
    return this.http.patch<void>(`${this.API}/${id}/deactivate`, {}).pipe(
      tap(() => this.updateLocalStatus(id, false))
    );
  }

  private updateLocalStatus(id: number, active: boolean) {
    this._projects.update(list =>
      list.map(p => p.id === id ? { ...p, active } : p)
    );
  }

  createProject(teacherId: number, request: ProjectCreateRequest) {
    return this.http.post<ProjectResponse>(`${this.API}/teacher/${teacherId}`, request).pipe(
      tap(newProj => {
        this._projects.update(all => [newProj, ...all]);
      })
    );
  }
}
