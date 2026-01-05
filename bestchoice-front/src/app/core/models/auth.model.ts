export type Role = 'ETUDIANT' | 'ENSEIGNANT' | 'ADMIN';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  expiresIn: number;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  studentNumber?: string | null;
  role: Role;
}

export interface RegisterResponse {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  message: string;
}
