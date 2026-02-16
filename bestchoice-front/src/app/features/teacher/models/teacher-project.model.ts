import { WorkType } from '../../../core/models/enums.model';

export interface ProjectResponse {
  id: number;
  title: string;
  description: string;
  workType: WorkType;
  remotePossible: boolean;
  active: boolean;
  minStudents: number;
  maxStudents: number;
  complet: boolean;
  teacherId: number;
  teacherName: string;
  requiredSkills: string[]; // Set<String> en Java
  keywords: string[];       // Set<String> en Java
  assignedStudentEmails: string[];
}

export interface ProjectCreateRequest {
  title: string;
  description: string;
  workType: WorkType;
  remotePossible: boolean;
  minStudents: number;
  maxStudents: number;
  requiredSkill: string[];
  keyword: string[];
}
