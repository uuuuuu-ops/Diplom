import React, { useState } from 'react';
import { CourseDraft } from '../../types/createCourse';
import { createCourse, createLesson, createQuiz } from '../../api/createCourse';

interface Props {
  draft: CourseDraft;
  updateDraft: (patch: Partial<CourseDraft>) => void;
  toast: (msg: string, type?: 'ok' | 'err' | 'info') => void;
  onBack: () => void;
  onDone: () => void;
}

const StepPublish: React.FC<Props> = ({ draft, updateDraft, toast, onBack, onDone }) => {
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const totalQ   = draft.lessons.reduce((s, l) => s + l.quiz.length, 0);
  const totalMin = draft.lessons.reduce((s, l) => s + (parseInt(l.duration || '0', 10) || 0), 0);

  const handlePublish = async () => {
    setLoading(true);
    try {
      const courseRes = await createCourse({
        title:         draft.title,
        description:   draft.description,
        category:      draft.category,
        level:         draft.level || undefined,
        published:     draft.visibility === 'published',
        free:          draft.free,
        thumbnailFile: draft.thumbnailFile ?? null,
      });
      const courseId = courseRes.data.id;
      const isPublished = draft.visibility === 'published';

      for (const lesson of draft.lessons) {
        const form = new FormData();
        form.append('title', lesson.title);
        form.append('orderIndex', String(lesson.orderIndex));
        form.append('duration', String(Math.max(1, parseInt(lesson.duration || '1', 10))));
        if (lesson.description) form.append('description', lesson.description);
        if (lesson.contentType === 'text' && lesson.text)
          form.append('lectureText', lesson.text);
        if (lesson.contentType === 'video' && lesson.videoFile)
          form.append('videoFile', lesson.videoFile);
        if (lesson.contentType === 'file' && lesson.pdfFile)
          form.append('lecturePdfFile', lesson.pdfFile);
        if (lesson.quiz.length > 0)
          form.append('quizRequired', 'true');
        form.append('published', String(isPublished));

        const lessonRes = await createLesson(courseId, form);
        const lessonId = lessonRes.data.id;

        if (lesson.quiz.length > 0) {
          await createQuiz(lessonId, {
            title:       `${lesson.title} Quiz`,
            description: `Quiz for lesson: ${lesson.title}`,
            passingScore: 70,
            published:   isPublished,
            questions:   lesson.quiz.map(q => ({
              question:           q.text,
              options:            [...q.answers],
              correctAnswerIndex: q.correct,
            })),
          });
        }
      }

      setSubmitted(true);
      toast('Course created successfully!', 'ok');
      setTimeout(onDone, 1500);
    } catch (err: any) {
      toast(err?.response?.data?.message || 'Failed to create course', 'err');
    } finally {
      setLoading(false);
    }
  };

  const reviewRows: [string, string, boolean][] = [
    ['Title',       draft.title       || '—', !draft.title],
    ['Category',    draft.category    || '—', !draft.category],
    ['Description', draft.description ? draft.description.slice(0, 70) + '…' : '—', !draft.description],
    ['Duration',    draft.duration     ? `${draft.duration} hrs` : '—', !draft.duration],
  ];

  return (
    <>
      <div className="cc-summary-grid">
        {[
          { label: 'Lessons',        val: draft.lessons.length },
          { label: 'Quiz questions', val: totalQ },
          { label: 'Total duration', val: totalMin ? `${totalMin}min` : '—' },
          { label: 'Level',          val: draft.level || '—' },
        ].map(({ label, val }) => (
          <div key={label} className="cc-stat">
            <div className="cc-stat-label">{label}</div>
            <div className={`cc-stat-val ${String(val).length > 6 ? 'cc-stat-val--sm' : ''}`}>
              {val}
            </div>
          </div>
        ))}
      </div>

      <div className="cc-card">
        <div className="cc-card-head">
          <div className="cc-card-head-left">
            <div>
              <div className="cc-card-label">Review</div>
              <div className="cc-card-desc">Check everything before publishing</div>
            </div>
          </div>
          <button className="cc-btn cc-btn--outline cc-btn--sm" onClick={onBack}>
            Edit ↗
          </button>
        </div>

        <div className="cc-card-body" style={{ paddingTop: '0.5rem', paddingBottom: '0.5rem' }}>
          {reviewRows.map(([key, val, dim]) => (
            <div key={key} className="cc-review-row">
              <span className="cc-review-key">{key}</span>
              <span className={`cc-review-val ${dim ? 'cc-review-val--dim' : ''}`}>{val}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="cc-card">
        <div className="cc-card-head">
          <div className="cc-card-head-left">
            <div>
              <div className="cc-card-label">Access</div>
              <div className="cc-card-desc">How students can enroll</div>
            </div>
          </div>
        </div>

        <div className="cc-card-body">
          <div className="cc-level-picker" style={{ maxWidth: 320 }}>
            {([true, false] as const).map(isFree => (
              <div
                key={String(isFree)}
                className={`cc-level-opt ${draft.free === isFree ? 'cc-level-opt--active' : ''}`}
                onClick={() => updateDraft({ free: isFree })}
              >
                {isFree ? 'Free' : 'Subscription only'}
              </div>
            ))}
          </div>
          <div style={{ marginTop: '0.75rem', fontFamily: 'var(--font-mono)', fontSize: '0.82rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
            {draft.free
              ? 'Anyone can enroll at no cost.'
              : 'Only users with an active subscription can access this course.'}
          </div>
        </div>
      </div>

      <div className="cc-card">
        <div className="cc-card-head">
          <div className="cc-card-head-left">
            <div>
              <div className="cc-card-label">Visibility</div>
              <div className="cc-card-desc">Who can see this course</div>
            </div>
          </div>
        </div>

        <div className="cc-card-body">
          <div className="cc-level-picker" style={{ maxWidth: 320 }}>
            {(['draft', 'published'] as const).map(mode => (
              <div
                key={mode}
                className={`cc-level-opt ${draft.visibility === mode ? 'cc-level-opt--active' : ''}`}
                onClick={() => updateDraft({ visibility: mode })}
              >
                {mode.charAt(0).toUpperCase() + mode.slice(1)}
              </div>
            ))}
          </div>
          <div style={{ marginTop: '0.75rem', fontFamily: 'var(--font-mono)', fontSize: '0.82rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
            {draft.visibility === 'draft'
              ? 'Draft courses are only visible to you. You can publish anytime from your dashboard.'
              : 'This course will be publicly visible to all students immediately after creation.'}
          </div>
        </div>
      </div>

      <div className="cc-actions">
        <button className="cc-btn cc-btn--outline" onClick={onBack}>← Lessons</button>
        <div className="cc-actions-right">
          <button
            className="cc-btn cc-btn--accent"
            onClick={handlePublish}
            disabled={loading || submitted}
          >
            {loading ? 'Creating…' : submitted ? 'Created!' : 'Create course'}
          </button>
        </div>
      </div>
    </>
  );
};

export default StepPublish;