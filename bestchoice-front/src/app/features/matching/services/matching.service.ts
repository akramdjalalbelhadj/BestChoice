import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  MatchingResultResponse,
  MatchingRunResponse,
  MatchingRunRequest
} from '../models/matching.model';

@Injectable({
  providedIn: 'root'
})
export class MatchingService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiBaseUrl}/api/matching`;

  // ==================== EXÉCUTION (MatchingController) ====================

  /**
   * Lance le processus de matching global pour une campagne.
   * Route : POST /api/matching/campaign/{campaignId}/run
   */
  runMatching(campaignId: number): Observable<MatchingRunResponse> {
    return this.http.post<MatchingRunResponse>(`${this.API}/campaign/${campaignId}/run`, {});
  }

  // Note : Si ton backend évolue pour accepter le MatchingRunRequest dans le body :
  // runMatchingWithSettings(campaignId: number, request: MatchingRunRequest): Observable<MatchingRunResponse> {
  //   return this.http.post<MatchingRunResponse>(`${this.API}/campaign/${campaignId}/run`, request);
  // }

  // ==================== LECTURE DES RÉSULTATS (MatchingResultController) ====================

  /**
   * Récupère tous les scores d'une campagne.
   */
  getResultsByCampaign(campaignId: number): Observable<MatchingResultResponse[]> {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/campaign/${campaignId}`);
  }

  /**
   * Récupère les scores d'un étudiant précis pour une campagne.
   */
  getResultsByStudent(campaignId: number, studentId: number): Observable<MatchingResultResponse[]> {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/campaign/${campaignId}/student/${studentId}`);
  }

  /**
   * Récupère les N meilleurs résultats (Top) pour un étudiant.
   */
  getTopResultsForStudent(campaignId: number, studentId: number, n: number): Observable<MatchingResultResponse[]> {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/campaign/${campaignId}/student/${studentId}/top/${n}`);
  }

  /**
   * Récupère les candidats recommandés pour un projet donné.
   */
  getResultsByProject(campaignId: number, projectId: number): Observable<MatchingResultResponse[]> {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/campaign/${campaignId}/project/${projectId}`);
  }

  /**
   * Récupère les candidats recommandés pour une matière donnée.
   */
  getResultsBySubject(campaignId: number, subjectId: number): Observable<MatchingResultResponse[]> {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/campaign/${campaignId}/subject/${subjectId}`);
  }

  /**
   * Récupère un résultat unique par son ID.
   */
  getResultById(id: number): Observable<MatchingResultResponse> {
    return this.http.get<MatchingResultResponse>(`${this.API}/${id}`);
  }

  // ==================== SUPPRESSION ====================

  /**
   * Supprime tous les résultats d'une campagne (Reset).
   */
  deleteResultsByCampaign(campaignId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/campaign/${campaignId}`);
  }

  clearResults(campaignId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/campaign/${campaignId}`);
  }
}
