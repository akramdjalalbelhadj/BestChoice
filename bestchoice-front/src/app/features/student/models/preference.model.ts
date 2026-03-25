import { PreferenceStatus } from '../../../core/models/enums.model';

/** * Interface miroir de PreferenceCreateRequest.java
 *
 */
export interface PreferenceCreateRequest {
  studentId: number;
  campaignId: number;
  projectId: number;
  subjectId: number;
  rank: number;
  comment?: string;
}

/** * Interface miroir de PreferenceResponse.java
 *
 */
export interface PreferenceResponse {
  id: number;
  studentId: number;
  campaignId: number;
  projectId: number;
  subjectId: number;
  rank: number;
  status: PreferenceStatus;
  createdAt: string;
}
