import { WorkType } from '../../../core/models/enums.model';

export interface StudentResponse {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  studentNumber: string;
  program: string;
  studyYear: number;
  track: string;
  preferredWorkType: WorkType;
  skill: string[];
  interestKeyword: string[];
  githubUrl: string;
  linkedinUrl: string;
  assignedProjectId: number | null;
}

export interface StudentUpdateRequest {
  studyYear: number;
  preferredWorkType: WorkType;
  skill: string[];
  interestKeyword: string[];
  githubUrl: string;
  portfolioUrl: string;
  linkedinUrl: string;
}
