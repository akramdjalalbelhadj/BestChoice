import { MatchingAlgorithmType } from '../../../core/models/enums.model';
import { MatchingCampaignType } from '../models/matching-campaign-type.model';

export interface CampaignResponse {
  id: number;
  name: string;
  description: string;
  academicYear: string;
  semester: number;
  campaignType: MatchingCampaignType;
  algorithmType: MatchingAlgorithmType;
  skillsWeight: number;
  workTypeWeight: number;
  interestsWeight: number;
  teacherId: number;
  teacherName: string;
  studentsCount: number;
  itemsCount: number;
  createdAt: string;
}

export interface CampaignRequest {
  name: string;
  description: string;
  academicYear: string;
  semester: number;
  campaignType: MatchingCampaignType;
  algorithmType: MatchingAlgorithmType;
  skillsWeight: number;
  workTypeWeight: number;
  interestsWeight: number;
  teacherId: number;
}
