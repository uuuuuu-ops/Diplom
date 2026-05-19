export type ContentType = 'text' | 'file' | 'video';

export interface QuizQuestion {
  text: string;
  answers: string[];
  correct: number;
}

export interface LessonDraft {
  id: number;
  title: string;
  description: string;
  duration: string;
  orderIndex: number;
  contentType: ContentType;
  text: string;
  videoUrl: string;
  videoFile: File | null;
  videoFileName: string;
  pdfFile: File | null;
  pdfFileName: string;
  quiz: QuizQuestion[];
  quizOpen: boolean;
}

export interface CourseDraft {
  title: string;
  description: string;
  category: string;
  level: string;
  duration: string;
  thumbnail: string;
  thumbnailFile?: File;
  lessons: LessonDraft[];
  visibility: 'draft' | 'published';
  free: boolean;
}