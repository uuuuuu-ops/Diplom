import api from './index';
import type { Course } from './courses';

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  bio?: string;
  avatarUrl?: string;
  role?: string;
  courseCount?: number;
  enrolledCount?: number;
  socialLinks?: {
    twitter?: string;
    linkedin?: string;
    github?: string;
  };
}

export interface Review {
  id: string;
  userName?: string;
  rating: number;
  review?: string;
  courseTitle?: string;
  createdAt: string;
}

export interface UpdateProfileRequest {
  name?: string;
  bio?: string;
}

export interface TeacherApplication {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  specialization: string;
  yearsOfExperience: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  score?: number;
  aiSummary?: string;
  aiStrengths?: string;
  aiWeaknesses?: string;
  aiRecommendation?: string;
  reviewComment?: string;
  createdAt: string;
}

export interface TeacherApplicationFormData {
  fullName: string;
  email: string;
  specialization: string;
  yearsOfExperience: number;
  resumeFile: File;
}

export const getProfile = () => api.get<UserProfile>('/profile/me');

export const updateProfile = (data: UpdateProfileRequest) =>
  api.put<void>('/profile/me', data);

export const uploadAvatar = (file: File) => {
  const form = new FormData();
  form.append('file', file);
  return api.post<UserProfile>('/profile/me/avatar', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const submitTeacherApplication = (data: TeacherApplicationFormData) => {
  const form = new FormData();
  form.append('fullName', data.fullName);
  form.append('email', data.email);
  form.append('specialization', data.specialization);
  form.append('yearsOfExperience', String(data.yearsOfExperience));
  form.append('resumeFile', data.resumeFile);
  return api.post<TeacherApplication>('/teacher-applications', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const getMyApplication = () =>
  api.get<TeacherApplication>('/teacher-applications/me');

export const getMyReviews = () => api.get<Review[]>('/courses/ratings/my');

export interface PublicProfile {
  id: string;
  name: string;
  role?: string;
  profileImageUrl?: string;
  bio?: string;
  socialLinks?: { twitter?: string; linkedin?: string; github?: string; [key: string]: string | undefined };
  courses: Course[];
  reviews: Review[];
}

export const getPublicProfile = (userId: string) =>
  api.get<PublicProfile>(`/profile/${userId}`);

export default api;
