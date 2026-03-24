import { WorkType } from '../../../core/models/enums.model';

/**
 * Interface pour la réponse (GET)
 */
export interface ProjectResponse {
  id: number;
  title: string;
  description: string;
  workTypes: WorkType[];
  remotePossible: boolean;
  active: boolean;
  minStudents: number;
  maxStudents: number;
  complet: boolean;
  credits: number;
  semester: number;
  academicYear: string;
  targetProgram: string;
  teacherId: number;
  teacherName: string;
  requiredSkills: string[];
  keywords: string[];
  assignedStudentEmails: string[];
}

/**
 * Interface pour la création (POST)
 * Doit correspondre à ProjectCreateRequest.java
 */
export interface ProjectCreateRequest {
  title: string;
  description: string;
  workTypes: WorkType[];
  remotePossible: boolean;
  minStudents: number;
  maxStudents: number;
  credits: number;
  semester: number;
  academicYear: string;
  targetProgram: string;
  requiredSkill: string[];
  keyword: string[];
}

/**
 * Interface pour la mise à jour (PATCH/PUT)
 */
export interface ProjectUpdateRequest {
  title?: string;
  description?: string;
  workTypes?: WorkType[];
  remotePossible?: boolean;
  minStudents?: number;
  maxStudents?: number;
  active?: boolean;
  requiredSkill?: string[];
  keyword?: string[];
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
