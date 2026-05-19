import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getLessonById,
  createLesson,
  updateLesson,
  getQuizByLessonId,
  createQuiz,
  updateQuiz,
  deleteQuiz,
  Lesson,
  Quiz,
  CreateQuizPayload,
} from '../api';
import { isAuthenticated } from '../api/auth';
import './css/EditLessonPage.css';

interface EditableQuestion {
  question: string;
  options: string[];
  correctAnswerIndex: number;
}

const defaultQuestion = (): EditableQuestion => ({
  question: '',
  options: ['', ''],
  correctAnswerIndex: 0,
});

const EditLessonPage: React.FC = () => {
  const { courseId, lessonId } = useParams<{ courseId: string; lessonId: string }>();
  const isNew = !lessonId || lessonId === 'new';
  const navigate = useNavigate();

  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [loading, setLoading] = useState(!isNew);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  // Lesson fields
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [orderIndex, setOrderIndex] = useState(0);
  const [duration, setDuration] = useState(0);
  const [lectureText, setLectureText] = useState('');
  const [published, setPublished] = useState(false);
  const [quizRequired, setQuizRequired] = useState(false);
  const [videoFile, setVideoFile] = useState<File | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);

  // Quiz state
  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [quizLoading, setQuizLoading] = useState(false);
  const [quizSaving, setQuizSaving] = useState(false);
  const [quizError, setQuizError] = useState('');
  const [quizTitle, setQuizTitle] = useState('');
  const [quizDescription, setQuizDescription] = useState('');
  const [passingScore, setPassingScore] = useState(70);
  const [timeLimit, setTimeLimit] = useState('');
  const [quizPublished, setQuizPublished] = useState(false);
  const [questions, setQuestions] = useState<EditableQuestion[]>([defaultQuestion()]);
  const [showQuizForm, setShowQuizForm] = useState(false);

  const videoInputRef = useRef<HTMLInputElement>(null);
  const pdfInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    if (isNew || !lessonId) return;
    setLoading(true);
    getLessonById(lessonId)
      .then((res) => {
        const l = res.data;
        setLesson(l);
        setTitle(l.title);
        setDescription(l.description || '');
        setOrderIndex(l.orderIndex);
        setDuration(l.duration || 0);
        setLectureText(l.lectureText || '');
        setPublished(l.published);
        setQuizRequired(l.quizRequired || false);
      })
      .catch(() => setError('Could not load lesson.'))
      .finally(() => setLoading(false));

    setQuizLoading(true);
    getQuizByLessonId(lessonId)
      .then((res) => {
        const q = res.data;
        setQuiz(q);
        setQuizTitle(q.title);
        setQuizDescription(q.description || '');
        setPassingScore(q.passingScore);
        setTimeLimit(q.timeLimitSeconds ? String(q.timeLimitSeconds) : '');
        setQuizPublished(q.published);
        if (q.questions?.length) {
          setQuestions(
            q.questions.map((qq) => ({
              question: qq.question,
              options: qq.options.length >= 2 ? qq.options : [...qq.options, ''],
              correctAnswerIndex: qq.correctAnswerIndex ?? 0,
            }))
          );
        }
        setShowQuizForm(true);
      })
      .catch(() => { /* no quiz yet */ })
      .finally(() => setQuizLoading(false));
  }, [lessonId, isNew]);

  const handleSave = async () => {
    if (!courseId) return;
    if (!title.trim()) { setError('Title is required.'); return; }
    setSaving(true);
    setError('');
    try {
      const fd = new FormData();
      fd.append('title', title.trim());
      fd.append('description', description);
      fd.append('orderIndex', String(orderIndex));
      fd.append('duration', String(duration));
      fd.append('lectureText', lectureText);
      fd.append('published', String(published));
      fd.append('quizRequired', String(quizRequired));
      if (videoFile) fd.append('videoFile', videoFile);
      if (pdfFile) fd.append('lecturePdfFile', pdfFile);

      if (isNew) {
        await createLesson(courseId, fd);
      } else {
        await updateLesson(lessonId!, fd);
      }
      navigate(`/courses/${courseId}/edit`);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Could not save lesson.');
    } finally {
      setSaving(false);
    }
  };

  const buildQuizPayload = (): CreateQuizPayload | null => {
    if (!quizTitle.trim()) { setQuizError('Quiz title is required.'); return null; }
    if (!quizDescription.trim()) { setQuizError('Quiz description is required.'); return null; }
    for (let i = 0; i < questions.length; i++) {
      const q = questions[i];
      if (!q.question.trim()) { setQuizError(`Question ${i + 1} text is required.`); return null; }
      if (q.options.some((o) => !o.trim())) { setQuizError(`All options in question ${i + 1} must be filled.`); return null; }
      if (q.options.length < 2) { setQuizError(`Question ${i + 1} needs at least 2 options.`); return null; }
    }
    return {
      title: quizTitle.trim(),
      description: quizDescription.trim(),
      passingScore,
      timeLimitSeconds: timeLimit ? Number(timeLimit) : null,
      published: quizPublished,
      questions: questions.map((q) => ({
        question: q.question.trim(),
        options: q.options.map((o) => o.trim()),
        correctAnswerIndex: q.correctAnswerIndex,
      })),
    };
  };

  const handleSaveQuiz = async () => {
    if (!lessonId) return;
    setQuizError('');
    const payload = buildQuizPayload();
    if (!payload) return;
    setQuizSaving(true);
    try {
      if (quiz) {
        const res = await updateQuiz(quiz.id, payload);
        setQuiz(res.data);
      } else {
        const res = await createQuiz(lessonId, payload);
        setQuiz(res.data);
      }
      setQuizError('');
    } catch (err: any) {
      setQuizError(err?.response?.data?.message || 'Could not save quiz.');
    } finally {
      setQuizSaving(false);
    }
  };

  const handleDeleteQuiz = async () => {
    if (!quiz) return;
    if (!window.confirm('Delete this quiz? Students will lose their attempts.')) return;
    try {
      await deleteQuiz(quiz.id);
      setQuiz(null);
      setShowQuizForm(false);
      setQuestions([defaultQuestion()]);
      setQuizTitle('');
      setQuizDescription('');
      setPassingScore(70);
      setTimeLimit('');
      setQuizPublished(false);
    } catch (err: any) {
      setQuizError(err?.response?.data?.message || 'Could not delete quiz.');
    }
  };

  // Question helpers
  const setQuestion = (qi: number, value: string) =>
    setQuestions((prev) => prev.map((q, i) => i === qi ? { ...q, question: value } : q));

  const setOption = (qi: number, oi: number, value: string) =>
    setQuestions((prev) =>
      prev.map((q, i) =>
        i === qi
          ? { ...q, options: q.options.map((o, j) => (j === oi ? value : o)) }
          : q
      )
    );

  const addOption = (qi: number) =>
    setQuestions((prev) =>
      prev.map((q, i) =>
        i === qi && q.options.length < 6 ? { ...q, options: [...q.options, ''] } : q
      )
    );

  const removeOption = (qi: number, oi: number) =>
    setQuestions((prev) =>
      prev.map((q, i) => {
        if (i !== qi || q.options.length <= 2) return q;
        const options = q.options.filter((_, j) => j !== oi);
        const correctAnswerIndex =
          q.correctAnswerIndex >= options.length
            ? options.length - 1
            : q.correctAnswerIndex === oi
            ? 0
            : q.correctAnswerIndex > oi
            ? q.correctAnswerIndex - 1
            : q.correctAnswerIndex;
        return { ...q, options, correctAnswerIndex };
      })
    );

  const setCorrect = (qi: number, oi: number) =>
    setQuestions((prev) =>
      prev.map((q, i) => (i === qi ? { ...q, correctAnswerIndex: oi } : q))
    );

  const addQuestion = () => setQuestions((prev) => [...prev, defaultQuestion()]);

  const removeQuestion = (qi: number) =>
    setQuestions((prev) => prev.length > 1 ? prev.filter((_, i) => i !== qi) : prev);

  if (loading) {
    return (
      <div className="edit-lesson-page">
        <Navbar />
        <p className="el-loading">Loading lesson...</p>
      </div>
    );
  }

  if (!isNew && !lesson && !loading) {
    return (
      <div className="edit-lesson-page">
        <Navbar />
        <p className="el-loading">Lesson not found.</p>
      </div>
    );
  }

  return (
    <div className="edit-lesson-page">
      <Navbar />
      <div className="el-container">
        <button className="back-btn" onClick={() => navigate(`/courses/${courseId}/edit`)}>
          ← Back to course
        </button>

        <header className="el-header">
          <h1>{isNew ? 'Add lesson' : 'Edit lesson'}</h1>
          <p className="el-sub">
            {isNew
              ? 'Fill in the details below to add a new lesson.'
              : 'Update lesson content, files, and settings.'}
          </p>
        </header>

        {error && <p className="el-error">{error}</p>}

        <section className="el-card">
          <h2>Lesson details</h2>

          <label className="el-field">
            <span>Title *</span>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="el-input"
              placeholder="e.g. Introduction to variables"
            />
          </label>

          <label className="el-field">
            <span>Description</span>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="el-input"
              placeholder="Brief description of what this lesson covers"
            />
          </label>

          <div className="el-field-row">
            <label className="el-field">
              <span>Order index</span>
              <input
                type="number"
                min={0}
                value={orderIndex}
                onChange={(e) => setOrderIndex(Number(e.target.value))}
                className="el-input"
              />
            </label>
            <label className="el-field">
              <span>Duration (min)</span>
              <input
                type="number"
                min={0}
                value={duration}
                onChange={(e) => setDuration(Number(e.target.value))}
                className="el-input"
              />
            </label>
          </div>

          <label className="el-field">
            <span>Lecture notes</span>
            <textarea
              rows={6}
              value={lectureText}
              onChange={(e) => setLectureText(e.target.value)}
              className="el-input"
              placeholder="Optional written lecture content shown below the video"
            />
          </label>

          <div className="el-toggles">
            <label className="el-toggle">
              <input
                type="checkbox"
                checked={published}
                onChange={(e) => setPublished(e.target.checked)}
              />
              <span>Published</span>
            </label>
            <label className="el-toggle">
              <input
                type="checkbox"
                checked={quizRequired}
                onChange={(e) => setQuizRequired(e.target.checked)}
              />
              <span>Quiz required to complete lesson</span>
            </label>
          </div>
        </section>

        <section className="el-card">
          <h2>Media</h2>

          <div className="el-field">
            <span>Video file</span>
            <div className="el-file-row">
              {!videoFile && (lesson?.videoFileName || lesson?.videoUrl) && (
                <span className="el-file-existing">
                  {lesson.videoFileName
                    ? `Current: ${lesson.videoFileName.split('/').pop()}`
                    : 'External video URL set'}
                </span>
              )}
              {videoFile && <span className="el-file-existing">Selected: {videoFile.name}</span>}
              <button
                type="button"
                className="el-secondary-btn"
                onClick={() => videoInputRef.current?.click()}
              >
                {videoFile || lesson?.videoFileName || lesson?.videoUrl ? 'Replace video' : 'Upload video'}
              </button>
              <input
                ref={videoInputRef}
                type="file"
                accept="video/*"
                style={{ display: 'none' }}
                onChange={(e) => setVideoFile(e.target.files?.[0] || null)}
              />
            </div>
          </div>

          <div className="el-field">
            <span>Lecture PDF</span>
            <div className="el-file-row">
              {!pdfFile && lesson?.lecturePdfUrl && (
                <a
                  className="el-file-existing"
                  href={lesson.lecturePdfUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  View current PDF
                </a>
              )}
              {pdfFile && <span className="el-file-existing">Selected: {pdfFile.name}</span>}
              <button
                type="button"
                className="el-secondary-btn"
                onClick={() => pdfInputRef.current?.click()}
              >
                {pdfFile || lesson?.lecturePdfUrl ? 'Replace PDF' : 'Upload PDF'}
              </button>
              <input
                ref={pdfInputRef}
                type="file"
                accept="application/pdf"
                style={{ display: 'none' }}
                onChange={(e) => setPdfFile(e.target.files?.[0] || null)}
              />
            </div>
          </div>
        </section>

        {!isNew && (
          <section className="el-card el-quiz-card">
            <div className="el-quiz-header">
              <div>
                <h2>Quiz</h2>
                {quiz && (
                  <span className={`el-quiz-badge ${quiz.published ? 'published' : 'draft'}`}>
                    {quiz.published ? 'Published' : 'Draft'}
                  </span>
                )}
              </div>
              {!showQuizForm && !quizLoading && (
                <button
                  className="el-save-btn"
                  onClick={() => setShowQuizForm(true)}
                >
                  + Create quiz
                </button>
              )}
              {quiz && (
                <button className="el-danger-btn" onClick={handleDeleteQuiz}>
                  Delete quiz
                </button>
              )}
            </div>

            {quizLoading && <p className="el-quiz-empty">Loading quiz...</p>}

            {!quizLoading && !showQuizForm && (
              <p className="el-quiz-empty">No quiz for this lesson yet.</p>
            )}

            {showQuizForm && (
              <>
                {quizError && <p className="el-error">{quizError}</p>}

                {/* Quiz settings */}
                <label className="el-field">
                  <span>Quiz title *</span>
                  <input
                    type="text"
                    value={quizTitle}
                    onChange={(e) => setQuizTitle(e.target.value)}
                    className="el-input"
                    placeholder="e.g. Variables quiz"
                  />
                </label>

                <label className="el-field">
                  <span>Quiz description *</span>
                  <textarea
                    rows={2}
                    value={quizDescription}
                    onChange={(e) => setQuizDescription(e.target.value)}
                    className="el-input"
                    placeholder="Instructions for students"
                  />
                </label>

                <div className="el-field-row">
                  <label className="el-field">
                    <span>Passing score (%)</span>
                    <input
                      type="number"
                      min={0}
                      max={100}
                      value={passingScore}
                      onChange={(e) => setPassingScore(Number(e.target.value))}
                      className="el-input"
                    />
                  </label>
                  <label className="el-field">
                    <span>Time limit (seconds, blank = none)</span>
                    <input
                      type="number"
                      min={1}
                      value={timeLimit}
                      onChange={(e) => setTimeLimit(e.target.value)}
                      className="el-input"
                      placeholder="e.g. 300"
                    />
                  </label>
                </div>

                <label className="el-toggle">
                  <input
                    type="checkbox"
                    checked={quizPublished}
                    onChange={(e) => setQuizPublished(e.target.checked)}
                  />
                  <span>Published (visible to students)</span>
                </label>

                {/* Questions */}
                <div className="el-questions-section">
                  <div className="el-questions-header">
                    <h3>Questions ({questions.length})</h3>
                    <button
                      type="button"
                      className="el-secondary-btn"
                      onClick={addQuestion}
                    >
                      + Add question
                    </button>
                  </div>

                  {questions.map((q, qi) => (
                    <div key={qi} className="el-question-block">
                      <div className="el-question-title-row">
                        <span className="el-question-num">Q{qi + 1}</span>
                        {questions.length > 1 && (
                          <button
                            type="button"
                            className="el-remove-btn"
                            onClick={() => removeQuestion(qi)}
                          >
                            Remove
                          </button>
                        )}
                      </div>

                      <label className="el-field">
                        <span>Question text *</span>
                        <input
                          type="text"
                          value={q.question}
                          onChange={(e) => setQuestion(qi, e.target.value)}
                          className="el-input"
                          placeholder="e.g. What keyword declares a constant in Java?"
                        />
                      </label>

                      <div className="el-field">
                        <span className="el-label">Answer options</span>
                        <div className="el-options-list">
                          {q.options.map((opt, oi) => (
                            <div key={oi} className="el-option-row">
                              <input
                                type="radio"
                                name={`correct-${qi}`}
                                checked={q.correctAnswerIndex === oi}
                                onChange={() => setCorrect(qi, oi)}
                                className="el-radio"
                                title="Mark as correct answer"
                              />
                              <input
                                type="text"
                                value={opt}
                                onChange={(e) => setOption(qi, oi, e.target.value)}
                                className="el-input el-option-input"
                                placeholder={`Option ${oi + 1}`}
                              />
                              {q.options.length > 2 && (
                                <button
                                  type="button"
                                  className="el-remove-btn"
                                  onClick={() => removeOption(qi, oi)}
                                >
                                  ✕
                                </button>
                              )}
                            </div>
                          ))}
                        </div>
                        <p className="el-options-hint">
                          Select the radio button next to the correct answer.
                        </p>
                        {q.options.length < 6 && (
                          <button
                            type="button"
                            className="el-secondary-btn el-add-option-btn"
                            onClick={() => addOption(qi)}
                          >
                            + Add option
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>

                <div className="el-actions">
                  <button
                    className="el-save-btn"
                    disabled={quizSaving}
                    onClick={handleSaveQuiz}
                  >
                    {quizSaving ? 'Saving...' : quiz ? 'Save quiz' : 'Create quiz'}
                  </button>
                </div>
              </>
            )}
          </section>
        )}

        <div className="el-actions">
          <button className="el-save-btn" disabled={saving} onClick={handleSave}>
            {saving ? 'Saving...' : isNew ? 'Add lesson' : 'Save changes'}
          </button>
          <button className="el-secondary-btn" onClick={() => navigate(`/courses/${courseId}/edit`)}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default EditLessonPage;
