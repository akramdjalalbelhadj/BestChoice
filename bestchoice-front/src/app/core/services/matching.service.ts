import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {
  MatchingRunRequest,
  MatchingRunResponse,
  MatchingResultResponse
} from '../../features/matching/models/matching.model';

@Injectable({ providedIn: 'root' })
export class MatchingService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiBaseUrl}/api/matching`;

  // ==================== EXÉCUTION (MatchingController) ====================

  /** Lance l'algorithme (Weighted, Stable ou Hybrid) */
  runMatching(request: MatchingRunRequest) {
    return this.http.post<MatchingRunResponse>(`${this.API}/run`, request);
  }

  /** Force le re-calcul complet */
  recomputeMatching(request: MatchingRunRequest) {
    return this.http.post<MatchingRunResponse>(`${this.API}/recompute`, request);
  }

  // ==================== LECTURE (MatchingResultController) ====================

  /** Récupère tous les scores pour un projet (Fix pour ton dashboard) */
  getResultsByProject(projectId: number) {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/project/${projectId}`);
  }

  /** Récupère le Top N des étudiants pour un projet spécifique */
  getTopStudentsForProject(projectId: number, n: number = 5) {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/project/${projectId}/top/${n}`);
  }

  /** Récupère les recommandations (Top N) pour un étudiant */
  getRecommendations(studentId: number, n: number = 5) {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/student/${studentId}/top/${n}`);
  }

  /** Récupère tous les résultats d'une session (historique) */
  getResultsBySession(sessionId: string) {
    return this.http.get<MatchingResultResponse[]>(`${this.API}/session/${sessionId}`);
  }

  /** Supprime les résultats d'une session */
  deleteSession(sessionId: string) {
    return this.http.delete<void>(`${this.API}/session/${sessionId}`);
  }
}
