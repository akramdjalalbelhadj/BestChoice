import { WorkType } from '../../../core/models/enums.model';

export interface SubjectResponse {
  id: number;
  title: string;
  description: string;
  objectives: string;
  workTypes: WorkType[];
  maxStudents: number;
  minStudents: number;
  credits: number;
  semester: number;
  academicYear: string;
  active: boolean;
  teacherId: number;
  teacherName: string;
  requiredSkills: string[];
  keywords: string[];
}

export interface SubjectCreateRequest {
  title: string;
  description: string;
  objectives?: string;
  workTypes: WorkType[];
  maxStudents: number;
  minStudents: number;
  credits: number;
  semester: number;
  academicYear: string;
  requiredSkill: string[];
  keyword: string[];
}

export interface SubjectUpdateRequest extends Partial<SubjectCreateRequest> {
  active?: boolean;
}
