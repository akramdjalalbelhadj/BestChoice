import { PreferenceStatus } from '../../../core/models/enums.model';

/** * Interface miroir de PreferenceCreateRequest.java
 *
 */
export interface PreferenceCreateRequest {
  studentId: number;
  projectId: number;
  rank: number;
  motivation?: string;
  comment?: string;
}

/** * Interface miroir de PreferenceResponse.java
 *
 */
export interface PreferenceResponse {
  id: number;
  studentId: number;
  projectId: number;
  projectTitle?: string;
  rank: number;
  status: PreferenceStatus;
  createdAt: string;
}
