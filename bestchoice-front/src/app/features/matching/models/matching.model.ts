import { MatchingAlgorithmType } from '../../../core/models/enums.model';

export type MatchingScope = 'ALL_STUDENTS' | 'ONE_STUDENT';

export interface MatchingResultResponse {
  id: number;
  studentId: number;
  campaignId: number;
  projectId: number | null;
  subjectId: number | null;
  globalScore: number;
  skillsScore: number;
  interestsScore: number;
  recommendationRank: number;
  algorithmUsed: MatchingAlgorithmType;
  calculationDate: string;
  studentName?: string;
  projectName?: string;
  subjectName?: string;
}

/**
 * Requête pour lancer le matching
 */
export interface MatchingRunRequest {
  algorithm: MatchingAlgorithmType;
  scope: MatchingScope;
  studentId?: number | null;
  recompute: boolean;
  persist: boolean;
  threshold?: number;
  weights?: { [key: string]: number };
}

/** * Réponse après exécution
 */
export interface MatchingRunResponse {
  campaignId: number;
  algorithmUsed: MatchingAlgorithmType;
  studentsProcessed: number;
  startedAt: string;
  finishedAt: string;
}
