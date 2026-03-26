import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { ProjectResponse } from '../../project/models/project.model';
import { StudentResponse, StudentUpdateRequest } from '../models/student.model';
import { MatchingResultResponse } from '../../matching/models/matching.model';
import { PreferenceCreateRequest, PreferenceResponse } from '../models/preference.model';
import { CampaignService } from '../../campaign/services/campaign.service';
import { ProjectService } from '../../project/services/project.service';
import { SubjectService } from '../../subject/services/subject.service';
import { SubjectResponse } from '../../subject/models/subject.model';
import {Observable, of, switchMap, tap} from 'rxjs';
import {CampaignResponse} from '../../campaign/models/campaign.model';
import {catchError} from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private http = inject(HttpClient);
  private campaignService = inject(CampaignService);
  private projectService = inject(ProjectService);
  private subjectService = inject(SubjectService);
  private readonly API = `${environment.apiBaseUrl}/api/students`;
  private readonly PREF_API = `${environment.apiBaseUrl}/api/preferences`;

  // --- ÉTAT DU SERVICE (SIGNALS PRIVÉS) ---
  private readonly _studentProfile = signal<StudentResponse | null>(null);
  private readonly _campaigns = signal<CampaignResponse[]>([]);
  private readonly _topMatches = signal<MatchingResultResponse[]>([]);

  // --- EXPOSITION PUBLIQUE ---
  readonly studentProfile = this._studentProfile.asReadonly();
  readonly campaigns = this._campaigns.asReadonly();
  readonly topMatches = this._topMatches.asReadonly();


  /**
   * Reset complet des données (Appelé lors de la déconnexion)
   */
  clearData() {
    this._studentProfile.set(null);
    this._campaigns.set([]);
    this._topMatches.set([]);
  }

  /**
   * Charge le profil ET les campagnes de l'étudiant de manière atomique
   */
  loadInitialData(userId: number): Observable<CampaignResponse[]> {
    return this.http.get<StudentResponse>(`${this.API}/user/${userId}`).pipe(
      tap(student => this._studentProfile.set(student)),
      switchMap(student => {
        if (!student.id) return of([]);
        return this.loadMyCampaigns(student.id);
      }),
      catchError(err => {
        console.error('Erreur initialisation étudiant', err);
        return of([]);
      })
    );
  }

  /** Charge les campagnes via le CampaignService spécialisé */
  loadMyCampaigns(studentId: number): Observable<CampaignResponse[]> {
    return this.campaignService.loadByStudent(studentId).pipe(
      tap(list => this._campaigns.set(list))
    );
  }

  getAllStudents(): Observable<StudentResponse[]> {
    return this.http.get<StudentResponse[]>(this.API);
  }

  /** Récupère le profil complet de l'étudiant */
  loadProfile(userId: number) {
    return this.http.get<StudentResponse>(`${this.API}/user/${userId}`).pipe(
      tap(res => this._studentProfile.set(res))
    );
  }

  /** Récupère les top recommandations */
  loadTopMatches(studentId: number) {
    return this.http.get<MatchingResultResponse[]>(`${environment.apiBaseUrl}/api/matching/student/${studentId}`).pipe(
      tap(res => this._topMatches.set(res))
    );
  }

  /** Ajoute un projet aux vœux de l'étudiant */
  addToPreferences(studentId: number, projectId: number, rank: number) {
    const request = { studentId, projectId, rank, motivation: "Choix via catalogue" };
    return this.http.post(`${this.PREF_API}`, request);
  }

  /** Récupère un projet spécifique par ID */
  getProjectById(id: number) {
    return this.http.get<ProjectResponse>(`${this.API}/projects/${id}`);
  }

  /** * Récupère une matière spécifique via le SubjectService
   */
  getSubjectById(id: number): Observable<SubjectResponse> {
    return this.subjectService.getById(id);
  }

  /** Récupère les préférences d'un étudiant */
  getPreferences(studentId: number) {
    return this.http.get<PreferenceResponse[]>(`${this.PREF_API}/student/${studentId}`);
  }

  /** Supprime une préférence */
  deletePreference(id: number) {
    return this.http.delete(`${this.PREF_API}/${id}`);
  }

  // --- MISE À JOUR PROFIL ---
  updateProfile(studentId: number, request: StudentUpdateRequest) {
    return this.http.put<StudentResponse>(`${this.API}/${studentId}`, request).pipe(
      tap(res => this._studentProfile.set(res))
    );
  }

  /** Récupère toutes les compétences actives du catalogue */
  getAllActiveSkills() {
    return this.http.get<any[]>(`${environment.apiBaseUrl}/api/skills/active`);
  }

  /** Récupère tous les mots-clés actifs du catalogue */
  getAllActiveKeywords() {
    return this.http.get<any[]>(`${environment.apiBaseUrl}/api/keywords/active`);
  }


  updatePreferencesOrder(updates: {id: number, rank: number}[]): Observable<void> {
    return this.http.put<void>(`${this.PREF_API}/preferences/bulk-rank`, updates);
  }

  submitPreference(request: any): Observable<any> {
    return this.http.post(`${this.PREF_API}`, request);
  }

  getPreferencesByCampaign(studentId: number, campaignId: number): Observable<PreferenceResponse[]> {
    return this.http.get<PreferenceResponse[]>(`${this.PREF_API}/student/${studentId}/campaign/${campaignId}`);
  }

}
