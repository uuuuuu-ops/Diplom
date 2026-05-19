import React from 'react';
import { LessonDraft } from '../../types/createCourse';
import LessonCard from './LessonCard';
import './LessonCard.css';

interface Props {
  lessons: LessonDraft[];
  updateLesson: (id: number, patch: Partial<LessonDraft>) => void;
  addLesson: () => void;
  deleteLesson: (id: number) => void;
  toast: (msg: string, type?: 'ok' | 'err' | 'info') => void;
  onBack: () => void;
  onNext: () => void;
}

const StepLessons: React.FC<Props> = ({
  lessons, updateLesson, addLesson, deleteLesson, toast, onBack, onNext,
}) => (
  <>
    <div className="cc-card">
      <div className="cc-card-head">
        <div className="cc-card-head-left">
          <div>
            <div className="cc-card-label">
              Lessons
              <span style={{
                fontFamily: 'var(--font-mono)', fontSize: '0.78rem',
                color: 'var(--text-muted)', fontWeight: 400, fontStyle: 'italic', marginLeft: 8,
              }}>
                ({lessons.length})
              </span>
            </div>
            <div className="cc-card-desc">
              Each lesson has content (text, file, or video) and an optional quiz at the end
            </div>
          </div>
        </div>
        <button className="cc-btn cc-btn--dark cc-btn--sm" onClick={addLesson}>
          + Add lesson
        </button>
      </div>

      <div className="cc-card-body" style={{ paddingBottom: '0.75rem' }}>
        {lessons.length === 0 ? (
          <div className="cc-empty">No lessons yet — add your first one above</div>
        ) : (
          lessons.map((lesson, i) => (
            <LessonCard
              key={lesson.id}
              lesson={lesson}
              index={i}
              onChange={patch => updateLesson(lesson.id, patch)}
              onDelete={() => deleteLesson(lesson.id)}
              toast={toast}
            />
          ))
        )}

        <button
          className="cc-btn-add-dashed"
          onClick={addLesson}
          style={{ marginTop: '0.25rem' }}
        >
          <span style={{ fontSize: '1.1rem', lineHeight: 1, fontStyle: 'normal' }}>+</span>
          Add another lesson
        </button>
      </div>
    </div>

    <div className="cc-actions">
      <button className="cc-btn cc-btn--outline" onClick={onBack}>← Basics</button>
      <button className="cc-btn cc-btn--accent" onClick={onNext}>Review &amp; Publish →</button>
    </div>
  </>
);

export default StepLessons;
