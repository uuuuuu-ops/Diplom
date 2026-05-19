import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import StepBasics from '../components/createCourse/StepBasics';
import StepLessons from '../components/createCourse/StepLessons';
import StepPublish from '../components/createCourse/StepPublish';
import { CourseDraft, LessonDraft } from '../types/createCourse';
import './css/CreateCoursePage.css';

const STEPS = ['Basics', 'Lessons & Quizzes', 'Publish'] as const;

const defaultDraft = (): CourseDraft => ({
  title: '',
  description: '',
  category: '',
  level: '',
  duration: '',
  thumbnail: '',
  lessons: [],
  visibility: 'published',
  free: true,
});

const CreateCoursePage: React.FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [draft, setDraft] = useState<CourseDraft>(defaultDraft);
  const [toasts, setToasts] = useState<{ id: number; msg: string; type: 'ok' | 'err' | 'info' }[]>([]);
  let toastId = 0;

  const toast = useCallback((msg: string, type: 'ok' | 'err' | 'info' = 'info') => {
    const id = ++toastId;
    setToasts(prev => [...prev, { id, msg, type }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 3500);
  }, []);

  const updateDraft = useCallback((patch: Partial<CourseDraft>) => {
    setDraft(prev => ({ ...prev, ...patch }));
  }, []);

  const updateLesson = useCallback((id: number, patch: Partial<LessonDraft>) => {
    setDraft(prev => ({
      ...prev,
      lessons: prev.lessons.map(l => l.id === id ? { ...l, ...patch } : l),
    }));
  }, []);

  const addLesson = useCallback(() => {
    const newLesson: LessonDraft = {
      id: Date.now(),
      title: '',
      description: '',
      duration: '',
      orderIndex: draft.lessons.length + 1,
      contentType: 'text',
      text: '',
      videoUrl: '',
      videoFile: null,
      videoFileName: '',
      pdfFile: null,
      pdfFileName: '',
      quiz: [],
      quizOpen: false,
    };
    setDraft(prev => ({ ...prev, lessons: [...prev.lessons, newLesson] }));
    toast('New lesson added');
  }, [draft.lessons.length, toast]);

  const deleteLesson = useCallback((id: number) => {
    setDraft(prev => ({
      ...prev,
      lessons: prev.lessons
        .filter(l => l.id !== id)
        .map((l, i) => ({ ...l, orderIndex: i + 1 })),
    }));
    toast('Lesson removed');
  }, [toast]);

  const validateStep0 = () => {
    if (!draft.title.trim())    { toast('Please add a course title', 'err'); return false; }
    if (!draft.description.trim()) { toast('Please add a description', 'err'); return false; }
    if (!draft.category)        { toast('Please pick a category', 'err'); return false; }
    if (!draft.level)           { toast('Please select a level', 'err'); return false; }
    return true;
  };

  const goTo = (n: number) => {
    if (n > step && step === 0 && !validateStep0()) return;
    setStep(n);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleReset = () => {
    if (window.confirm('Reset everything? All data will be lost.')) {
      setDraft(defaultDraft());
      setStep(0);
    }
  };

  return (
    <div className="cc-page">
      <Navbar />

      {/* Toast container */}
      <div className="cc-toasts">
        {toasts.map(t => (
          <div key={t.id} className={`cc-toast cc-toast--${t.type}`}>
            {t.msg}
          </div>
        ))}
      </div>

      <div className="cc-wrap">
        {/* Header */}
        <div className="cc-header">
          <div className="cc-header-left">
            <span className="cc-header-title">Create course</span>
          </div>
          <button className="cc-btn cc-btn--outline cc-btn--sm" onClick={handleReset}>
            ✕ Reset
          </button>
        </div>

        <div className="cc-layout">
          <div className="cc-sidebar">
            <div className="cc-steps">
              {STEPS.map((label, i) => (
                <button
                  key={i}
                  className={[
                    'cc-step-btn',
                    i === step ? 'cc-step-btn--active' : '',
                    i < step  ? 'cc-step-btn--done'   : '',
                  ].join(' ')}
                  onClick={() => goTo(i)}
                >
                  <span className="cc-step-num">{i < step ? '✓' : i + 1}</span>
                  <span className="cc-step-label">{label}</span>
                </button>
              ))}
            </div>
            <div className="cc-progress">
              <div
                className="cc-progress-fill"
                style={{ width: `${((step + 1) / 3) * 100}%` }}
              />
            </div>
          </div>

          {/* Right main: step panels */}
          <div className="cc-main">
            {step === 0 && (
              <StepBasics
                draft={draft}
                updateDraft={updateDraft}
                onNext={() => goTo(1)}
              />
            )}
            {step === 1 && (
              <StepLessons
                lessons={draft.lessons}
                updateLesson={updateLesson}
                addLesson={addLesson}
                deleteLesson={deleteLesson}
                toast={toast}
                onBack={() => goTo(0)}
                onNext={() => goTo(2)}
              />
            )}
            {step === 2 && (
              <StepPublish
                draft={draft}
                updateDraft={updateDraft}
                toast={toast}
                onBack={() => goTo(1)}
                onDone={() => navigate('/courses')}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreateCoursePage;
