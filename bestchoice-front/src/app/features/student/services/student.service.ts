import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ProjectResponse } from '../../project/models/project.model';
import { StudentResponse, StudentUpdateRequest } from '../models/student.model';
import { MatchingResultResponse } from '../../matching/models/matching.model';
import { PreferenceCreateRequest, PreferenceResponse } from '../models/preference.model';
import {Observable, tap} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiBaseUrl}/api/students`;

  studentProfile = signal<StudentResponse | null>(null);
  availableProjects = signal<ProjectResponse[]>([]);
  topMatches = signal<MatchingResultResponse[]>([]);

  getAllStudents(): Observable<StudentResponse[]> {
    return this.http.get<StudentResponse[]>(this.API);
  }

  /** Récupère le profil complet de l'étudiant */
  loadProfile(userId: number) {
    return this.http.get<StudentResponse>(`${this.API}/students/user/${userId}`).pipe(
      tap(res => this.studentProfile.set(res))
    );
  }

  /** Récupère les projets disponibles pour le matching */
  loadAvailableProjects() {
    return this.http.get<ProjectResponse[]>(`${this.API}/projects/available`).pipe(
      tap(res => this.availableProjects.set(res))
    );
  }

  /** Récupère les top recommandations de matching */
  loadTopMatches(studentId: number) {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/matching/student/${studentId}`).pipe(
      tap(res => this.topMatches.set(res))
    );
  }

  /** Ajoute un projet aux vœux de l'étudiant */
  addToPreferences(studentId: number, projectId: number, rank: number) {
    const request = { studentId, projectId, rank, motivation: "Choix via catalogue" };
    return this.http.post(`${this.API}/preferences`, request);
  }

  /** Récupère un projet spécifique par ID */
  getProjectById(id: number) {
    return this.http.get<ProjectResponse>(`${this.API}/projects/${id}`);
  }

  /** Crée une préférence (vœu) */
  submitPreference(request: PreferenceCreateRequest) {
    return this.http.post<PreferenceResponse>(`${this.API}/preferences`, request);
  }

  /** Récupère les préférences d'un étudiant */
  getPreferences(studentId: number) {
    return this.http.get<PreferenceResponse[]>(`${this.API}/preferences/student/${studentId}`);
  }

  /** Supprime une préférence */
  deletePreference(id: number) {
    return this.http.delete(`${this.API}/preferences/${id}`);
  }

  /** Met à jour le profil étudiant */
  updateProfile(studentId: number, request: StudentUpdateRequest) {
    return this.http.put<StudentResponse>(`${this.API}/students/${studentId}`, request).pipe(
      tap(res => this.studentProfile.set(res))
    );
  }

  /** Récupère toutes les compétences actives du catalogue */
  getAllActiveSkills() {
    return this.http.get<any[]>(`${this.API}/skills/active`);
  }

  /** Récupère tous les mots-clés actifs du catalogue */
  getAllActiveKeywords() {
    return this.http.get<any[]>(`${this.API}/keywords/active`);
  }
}
