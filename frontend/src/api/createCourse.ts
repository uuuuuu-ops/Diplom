import api from './index';
import type { Course, Lesson } from './courses';

export interface CreateCourseFields {
  title: string;
  description: string;
  category: string;
  level?: string;
  published?: boolean;
  free?: boolean;
  thumbnailFile?: File | null;
}

export const createCourse = (data: CreateCourseFields) => {
  const form = new FormData();
  form.append('title', data.title);
  form.append('description', data.description);
  form.append('category', data.category);
  if (data.level) form.append('level', data.level);
  if (data.published !== undefined) form.append('published', String(data.published));
  form.append('free', String(data.free ?? true));
  if (data.thumbnailFile) form.append('thumbnailFile', data.thumbnailFile);

  return api.post<Course>('/courses', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

export const createLesson = (courseId: string, formData: FormData) =>
  api.post<Lesson>(`/lessons/course/${courseId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

export interface CreateQuizData {
  title: string;
  description: string;
  passingScore?: number;
  published?: boolean;
  questions: {
    question: string;
    options: string[];
    correctAnswerIndex: number;
  }[];
}

export const createQuiz = (lessonId: string, data: CreateQuizData) =>
  api.post(`/quizzes/lesson/${lessonId}`, data);

export const updateCourse = (courseId: string, data: Partial<CreateCourseFields>) => {
  const form = new FormData();
  if (data.title) form.append('title', data.title);
  if (data.description) form.append('description', data.description);
  if (data.category) form.append('category', data.category);
  if (data.level) form.append('level', data.level);
  if (data.published !== undefined) form.append('published', String(data.published));
  if (data.free !== undefined) form.append('free', String(data.free));
  if (data.thumbnailFile) form.append('thumbnailFile', data.thumbnailFile);

  return api.put<Course>(`/courses/${courseId}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};