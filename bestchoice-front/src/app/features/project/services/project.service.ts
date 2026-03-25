import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ProjectResponse, ProjectCreateRequest, PageResponse, ProjectUpdateRequest } from '../models/project.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private readonly apiUrl = `${environment.apiBaseUrl}/api/projects`;

  constructor(private http: HttpClient) {}

  // ==================== LECTURE ====================

  /**
   * Récupère tous les projets (version simple non-paginée)
   */
  getAll(): Observable<ProjectResponse[]> {
    return this.http.get<ProjectResponse[]>(this.apiUrl);
  }

  /**
   * Récupère les projets avec pagination et tri
   */
  getPaginated(page: number = 0, size: number = 20, sortBy?: string, direction: string = 'ASC'): Observable<PageResponse<ProjectResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sortBy) {
      params = params.set('sortBy', sortBy).set('sortDirection', direction);
    }

    return this.http.get<PageResponse<ProjectResponse>>(`${this.apiUrl}/paginated`, { params });
  }

  getById(id: number): Observable<ProjectResponse> {
    return this.http.get<ProjectResponse>(`${this.apiUrl}/${id}`);
  }

  getAvailable(): Observable<ProjectResponse[]> {
    return this.http.get<ProjectResponse[]>(`${this.apiUrl}/available`);
  }

  getByTeacher(teacherId: number): Observable<ProjectResponse[]> {
    return this.http.get<ProjectResponse[]>(`${this.apiUrl}/teacher/${teacherId}`);
  }

  // ==================== ACTIONS ====================

  /**
   * Crée un projet pour un enseignant spécifique
   */
  create(teacherId: number, project: ProjectCreateRequest): Observable<ProjectResponse> {
    return this.http.post<ProjectResponse>(`${this.apiUrl}/teacher/${teacherId}`, project);
  }

  update(id: number, project: ProjectUpdateRequest): Observable<ProjectResponse> {
    return this.http.put<ProjectResponse>(`${this.apiUrl}/${id}`, project);
  }

  activate(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/activate`, {});
  }

  deactivate(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
