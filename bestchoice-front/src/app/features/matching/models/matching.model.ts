export type MatchingAlgorithmType = 'WEIGHTED' | 'STABLE' | 'HYBRID';
export type MatchingScope = 'ALL_STUDENTS' | 'ONE_STUDENT';

export interface MatchingResultResponse {
  id: number;
  sessionId: string;
  studentId: number;
  projectId: number;
  globalScore: number;
  skillsScore: number;
  interestsScore: number;
  calculationDate: string;
}

// Pour le Dashboard Teacher : on crée une interface utilitaire
// pour afficher les stats d'un projet spécifique
export interface TeacherProjectStats {
  projectId: number;
  projectTitle: number;
  preferenceCount: number;
  topMatches: MatchingResultResponse[];
}

export interface MatchingRunRequest {
  algorithm: MatchingAlgorithmType;
  scope: MatchingScope;
  studentId?: number | null; // Requis si scope = ONE_STUDENT
  recompute: boolean;
  persist: boolean;
  threshold?: number;        // ex: 0.5
  weights?: {                // ex: { skills: 0.6, interests: 0.4 }
    [key: string]: number;
  };
}

/** Réponse après exécution du calcul */
export interface MatchingRunResponse {
  sessionId: string;
  algorithmUsed: MatchingAlgorithmType;
  studentsProcessed: number;
  projectsConsidered: number;
  resultsComputed: number;
  resultsSaved: number;
  recompute: boolean;
  startedAt: string;  // ISO Date
  finishedAt: string; // ISO Date
  warnings: string[];
}
