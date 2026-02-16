export interface SkillResponse {
  id: number;
  name: string;
  description: string;
  category: string;
  level: number;
  active: boolean;
}

export interface KeywordResponse {
  id: number;
  label: string;
  description: string;
  domain: string;
  active: boolean;
}
