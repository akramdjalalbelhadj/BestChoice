import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SubjectResponse, SubjectCreateRequest, SubjectUpdateRequest } from '../models/subject.model';
import {MatchingRunRequest, MatchingRunResponse} from '../../matching/models/matching.model';

@Injectable({ providedIn: 'root' })
export class SubjectService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiBaseUrl}/api/subjects`;

  getByTeacher(teacherId: number): Observable<SubjectResponse[]> {
    return this.http.get<SubjectResponse[]>(`${this.API}/teacher/${teacherId}`);
  }

  getById(id: number): Observable<SubjectResponse> {
    return this.http.get<SubjectResponse>(`${this.API}/${id}`);
  }

  create(teacherId: number, request: SubjectCreateRequest): Observable<SubjectResponse> {
    return this.http.post<SubjectResponse>(`${this.API}/teacher/${teacherId}`, request);
  }

  update(id: number, request: SubjectUpdateRequest): Observable<SubjectResponse> {
    return this.http.put<SubjectResponse>(`${this.API}/${id}`, request);
  }

  activate(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/${id}/activate`, {});
  }

  deactivate(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/${id}/deactivate`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }

  runMatching(campaignId: number, request: MatchingRunRequest): Observable<MatchingRunResponse> {
    return this.http.post<MatchingRunResponse>(
      `${this.API}/campaign/${campaignId}/run`,
      request
    );
  }
}
