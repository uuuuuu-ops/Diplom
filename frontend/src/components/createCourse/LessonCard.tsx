import React, { useRef } from 'react';
import { LessonDraft, ContentType, QuizQuestion } from '../../types/createCourse';

interface Props {
  lesson: LessonDraft;
  index: number;
  onChange: (patch: Partial<LessonDraft>) => void;
  onDelete: () => void;
  toast: (msg: string, type?: 'ok' | 'err' | 'info') => void;
}

const LessonChips: React.FC<{ lesson: LessonDraft }> = ({ lesson }) => {
  const ct = { text: 'Text', file: 'File', video: 'Video' };
  return (
    <div className="lc-chips">
      <span className="lc-chip lc-chip--type">{ct[lesson.contentType]}</span>
      {lesson.duration && <span className="lc-chip lc-chip--dim">{lesson.duration}min</span>}
      {(lesson.videoFileName || lesson.videoUrl) && <span className="lc-chip lc-chip--file">Video ✓</span>}
      {lesson.pdfFileName && <span className="lc-chip lc-chip--file">File ✓</span>}
      {lesson.quiz.length > 0 && (
        <span className="lc-chip lc-chip--quiz">Quiz {lesson.quiz.length}q</span>
      )}
    </div>
  );
};

interface UploadZoneProps {
  accept: string;
  fileName: string;
  placeholder: string;
  hint: string;
  icon: string;
  onFile: (file: File) => void;
}
const UploadZone: React.FC<UploadZoneProps> = ({ accept, fileName, placeholder, hint, icon, onFile }) => {
  const ref = useRef<HTMLInputElement>(null);
  return (
    <div
      className={`cc-upload ${fileName ? 'cc-upload--done' : ''}`}
      onClick={() => ref.current?.click()}
    >
      <input
        ref={ref}
        type="file"
        accept={accept}
        style={{ position: 'absolute', opacity: 0, pointerEvents: 'none' }}
        onChange={e => { const f = e.target.files?.[0]; if (f) onFile(f); }}
      />
      <span className="cc-upload-icon">{fileName ? '✓' : icon}</span>
      <div className="cc-upload-label">{fileName || placeholder}</div>
      <div className="cc-upload-sub">{fileName ? 'Click to replace' : hint}</div>
    </div>
  );
};

interface QCardProps {
  q: QuizQuestion;
  index: number;
  onChange: (patch: Partial<QuizQuestion>) => void;
  onDelete: () => void;
}
const QCard: React.FC<QCardProps> = ({ q, index, onChange, onDelete }) => {
  const addAnswer = () => {
    if (q.answers.length >= 8) return;
    onChange({ answers: [...q.answers, ''] });
  };

  const deleteAnswer = (ai: number) => {
    const updated = q.answers.filter((_, i) => i !== ai);
    const newCorrect = updated.length === 0 ? 0 : q.correct >= updated.length ? updated.length - 1 : q.correct;
    onChange({ answers: updated, correct: newCorrect });
  };

  return (
    <div className="lc-q-card">
      <div className="lc-q-top">
        <span className="lc-q-idx">Q{index + 1}</span>
        <input
          className="cc-input"
          type="text"
          style={{ fontSize: '0.88rem', padding: '0.5rem 0.85rem' }}
          placeholder="Type your question…"
          value={q.text}
          onChange={e => onChange({ text: e.target.value })}
        />
        <button className="lc-q-del" onClick={onDelete}>✕</button>
      </div>

      {q.answers.length > 0 && (
        <div className="lc-ans-list">
          {q.answers.map((ans, ai) => (
            <div
              key={ai}
              className={`lc-ans-row ${q.correct === ai ? 'lc-ans-row--correct' : ''}`}
              onClick={() => onChange({ correct: ai })}
            >
              <div className={`lc-radio ${q.correct === ai ? 'lc-radio--on' : ''}`} />
              <input
                className="lc-ans-inp"
                type="text"
                placeholder={`Answer ${ai + 1}…`}
                value={ans}
                onClick={e => e.stopPropagation()}
                onChange={e => {
                  const updated = [...q.answers];
                  updated[ai] = e.target.value;
                  onChange({ answers: updated });
                }}
              />
              <button
                className="lc-ans-del"
                onClick={e => { e.stopPropagation(); deleteAnswer(ai); }}
              >✕</button>
            </div>
          ))}
        </div>
      )}

      <div className="lc-q-footer">
        {q.answers.length < 8 && (
          <button className="lc-ans-add" onClick={addAnswer}>+ Add answer</button>
        )}
        {q.answers.length > 0 && (
          <span className="lc-q-hint">Click an answer to mark it as correct</span>
        )}
      </div>
    </div>
  );
};

const LessonCard: React.FC<Props> = ({ lesson, index, onChange, onDelete, toast }) => {
  const isOpen = lesson.quizOpen;

  const setContentType = (type: ContentType) => onChange({ contentType: type });

  const handleVideoFile = (file: File) => {
    onChange({ videoFile: file, videoFileName: file.name });
    toast(`${file.name} attached`);
  };
  const handlePdfFile = (file: File) => {
    onChange({ pdfFile: file, pdfFileName: file.name });
    toast(`${file.name} attached`);
  };

  const addQuestion = () => {
    onChange({
      quiz: [...lesson.quiz, { text: '', answers: [], correct: 0 }],
      quizOpen: true,
    });
  };

  const updateQuestion = (qi: number, patch: Partial<QuizQuestion>) => {
    const updated = lesson.quiz.map((q, i) =>
      i === qi ? { ...q, ...patch } : q
    );
    onChange({ quiz: updated });
  };

  const deleteQuestion = (qi: number) => {
    onChange({ quiz: lesson.quiz.filter((_, i) => i !== qi) });
  };

  return (
    <div className="lc-card" id={`lesson-${lesson.id}`}>

      <div
        className="lc-head"
        onClick={() => {
          const body = document.getElementById(`lc-body-${lesson.id}`);
          if (body) body.style.display = body.style.display === 'none' ? '' : 'none';
          const chev = document.getElementById(`lc-chev-${lesson.id}`);
          if (chev) chev.classList.toggle('lc-chev--open');
        }}
      >
        <span className="lc-grab">⠿</span>
        <span className="lc-num">{String(index + 1).padStart(2, '0')}</span>
        <div className={`lc-name ${!lesson.title ? 'lc-name--empty' : ''}`}>
          {lesson.title || 'Untitled lesson'}
        </div>
        <LessonChips lesson={lesson} />
        <button
          className="lc-del"
          onClick={e => { e.stopPropagation(); onDelete(); }}
        >
          ✕
        </button>
        <span className="lc-chev" id={`lc-chev-${lesson.id}`}>▾</span>
      </div>

      <div className="lc-body" id={`lc-body-${lesson.id}`} style={{ display: 'none' }}>

        {/* 1. Lesson info */}
        <div className="lc-zone">
          <div className="lc-zone-label">
            <span className="lc-dot lc-dot--accent" />
            Lesson info
          </div>

          <div className="cc-grid-2" style={{ marginBottom: '0.9rem' }}>
            <div className="cc-field" style={{ marginBottom: 0 }}>
              <label className="cc-label">Title <span className="cc-label-req">*</span></label>
              <input
                className="cc-input"
                type="text"
                placeholder="e.g. What is a variable?"
                value={lesson.title}
                onChange={e => onChange({ title: e.target.value })}
              />
            </div>
            <div className="cc-field" style={{ marginBottom: 0 }}>
              <label className="cc-label">Duration (min)</label>
              <input
                className="cc-input"
                type="number"
                min={1}
                placeholder="e.g. 20"
                value={lesson.duration}
                onChange={e => onChange({ duration: e.target.value })}
              />
            </div>
          </div>

          <div className="cc-field" style={{ marginBottom: 0 }}>
            <label className="cc-label">Short description</label>
            <textarea
              className="cc-textarea"
              rows={2}
              placeholder="Brief summary of what's covered…"
              value={lesson.description}
              onChange={e => onChange({ description: e.target.value })}
            />
          </div>
        </div>

        {/* 2. Lesson content */}
        <div className="lc-zone">
          <div className="lc-zone-label">
            <span className="lc-dot lc-dot--accent" />
            Lesson content
          </div>

          {/* Content type tabs */}
          <div className="lc-ctabs">
            {(['text', 'file', 'video'] as ContentType[]).map(type => (
              <button
                key={type}
                className={`lc-ctab ${lesson.contentType === type ? 'lc-ctab--active' : ''}`}
                onClick={() => setContentType(type)}
              >
                <span>{type === 'text' ? '' : type === 'file' ? '' : ''}</span>
                <span className="lc-ctab-lbl">
                  {type === 'text' ? 'Text' : type === 'file' ? 'File / PDF' : 'Video'}
                </span>
              </button>
            ))}
          </div>

          {/* TEXT panel */}
          {lesson.contentType === 'text' && (
            <div>
              <textarea
                className="cc-textarea"
                rows={7}
                placeholder="Type or paste your lecture content here. Markdown is supported…"
                value={lesson.text}
                onChange={e => onChange({ text: e.target.value })}
              />
              <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.73rem', color: 'var(--text-muted)', marginTop: 4, fontStyle: 'italic' }}>
                This text will also be offered as a downloadable PDF to students.
              </div>
            </div>
          )}

          {/* FILE panel */}
          {lesson.contentType === 'file' && (
            <div>
              <UploadZone
                accept=".pdf,.doc,.docx,.ppt,.pptx"
                fileName={lesson.pdfFileName}
                placeholder="Upload lecture file"
                hint="PDF, Word, PowerPoint — max 100 MB"
                icon="PDF"
                onFile={handlePdfFile}
              />
              <div className="cc-field" style={{ marginTop: '0.9rem', marginBottom: 0 }}>
                <label className="cc-label">Notes for students (optional)</label>
                <textarea
                  className="cc-textarea"
                  rows={3}
                  placeholder="Add any extra notes shown alongside the file…"
                  value={lesson.text}
                  onChange={e => onChange({ text: e.target.value })}
                />
              </div>
            </div>
          )}

          {/* VIDEO panel */}
          {lesson.contentType === 'video' && (
            <div>
              <UploadZone
                accept="video/*"
                fileName={lesson.videoFileName}
                placeholder="Upload video file"
                hint="MP4, MOV, AVI, WebM — max 2 GB"
                icon="MP4"
                onFile={handleVideoFile}
              />
              <div className="cc-video-or">
                <hr /><span>or paste a URL</span><hr />
              </div>
              <input
                className="cc-input"
                type="url"
                placeholder="https://youtube.com/watch?v=… or direct .mp4 link"
                value={lesson.videoUrl}
                onChange={e => onChange({ videoUrl: e.target.value })}
              />
              <div className="cc-field" style={{ marginTop: '0.9rem', marginBottom: 0 }}>
                <label className="cc-label">Video notes / transcript (optional)</label>
                <textarea
                  className="cc-textarea"
                  rows={3}
                  placeholder="Add a summary, transcript or supplementary notes…"
                  value={lesson.text}
                  onChange={e => onChange({ text: e.target.value })}
                />
              </div>
            </div>
          )}
        </div>

        {/* 3. Quiz toggle */}
        <div
          className="lc-quiz-toggle"
          onClick={() => onChange({ quizOpen: !lesson.quizOpen })}
        >
          <div className="lc-quiz-toggle-left">
            <span style={{ fontStyle: 'normal' }}></span>
            Lesson quiz
            <span className="lc-quiz-badge">
              {lesson.quiz.length > 0
                ? `${lesson.quiz.length} question${lesson.quiz.length > 1 ? 's' : ''}`
                : 'optional'}
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            {lesson.quiz.length > 0 && (
              <button
                className="cc-btn cc-btn--dark cc-btn--sm"
                onClick={e => { e.stopPropagation(); addQuestion(); }}
              >
                + Question
              </button>
            )}
            <span className="lc-quiz-chev">{lesson.quizOpen ? '▴' : '▾'}</span>
          </div>
        </div>

        {/* Quiz panel */}
        {lesson.quizOpen && (
          <div className="lc-quiz-panel">
            {lesson.quiz.length === 0 ? (
              <div className="cc-empty" style={{ padding: '0.75rem 0' }}>
                No questions yet — students won't see a quiz unless you add one.
              </div>
            ) : (
              lesson.quiz.map((q, qi) => (
                <QCard
                  key={qi}
                  q={q}
                  index={qi}
                  onChange={patch => updateQuestion(qi, patch)}
                  onDelete={() => deleteQuestion(qi)}
                />
              ))
            )}
            <button className="cc-btn-add-inline" onClick={addQuestion} style={{ marginTop: '0.5rem' }}>
              <span style={{ fontStyle: 'normal' }}>+</span> Add question
            </button>
          </div>
        )}

      </div>
    </div>
  );
};

export default LessonCard;
