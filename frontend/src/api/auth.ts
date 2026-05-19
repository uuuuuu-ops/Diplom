import api from './index';

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface VerifyRequest {
  email: string;
  code: string;
}

export const register = (data: RegisterRequest) =>
  api.post<{ message: string }>('/auth/register', data);

export interface LoginResponse {
  token: string;
  role: string;
  teacherApproved: boolean;
  email: string;
  name: string;
}

export const login = (data: LoginRequest) =>
  api.post<LoginResponse>('/auth/login', data);

export const verify = (data: VerifyRequest) =>
  api.post<string>('/auth/verify', data);

export const resendCode = (data: Pick<VerifyRequest, 'email'>) =>
  api.post<{ message: string }>('/auth/resend-code', data);

export const logout = () => api.post<{ message: string }>('/auth/logout');

export const isAuthenticated = (): boolean => !!localStorage.getItem('token');

export const getUserRole = (): string | null => localStorage.getItem('role');

export const getUserId = (): string | null => {
  const token = localStorage.getItem('token');
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    return payload.sub ?? null;
  } catch {
    return null;
  }
};

export const clearToken = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
};

export default api;
