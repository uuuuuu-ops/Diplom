import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getQuiz,
  startQuiz,
  submitQuiz,
  getMyQuizAttempts,
  Quiz,
  QuizAttempt,
} from '../api';
import { isAuthenticated } from '../api/auth';
import './css/QuizPage.css';

const formatTime = (s: number): string => {
  const m = Math.floor(s / 60);
  const sec = s % 60;
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
};

const QuizPage: React.FC = () => {
  const { quizId } = useParams<{ quizId: string }>();
  const navigate = useNavigate();

  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [loading, setLoading] = useState(true);
  const [attempts, setAttempts] = useState<QuizAttempt[]>([]);

  const [phase, setPhase] = useState<'intro' | 'taking' | 'result'>('intro');
  const [answers, setAnswers] = useState<Record<number, number>>({});
  const [secondsLeft, setSecondsLeft] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<QuizAttempt | null>(null);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    if (!quizId) return;
    setLoading(true);
    Promise.allSettled([getQuiz(quizId), getMyQuizAttempts(quizId)]).then(([qRes, aRes]) => {
      setQuiz(qRes.status === 'fulfilled' ? qRes.value.data : null);
      setAttempts(aRes.status === 'fulfilled' ? aRes.value.data : []);
      setLoading(false);
    });
  }, [quizId]);

  // Timer tick
  useEffect(() => {
    if (phase !== 'taking' || secondsLeft === null) return;
    if (secondsLeft <= 0) {
      handleSubmit();
      return;
    }
    timerRef.current = window.setTimeout(() => {
      setSecondsLeft((s) => (s !== null ? s - 1 : null));
    }, 1000);
    return () => {
      if (timerRef.current) window.clearTimeout(timerRef.current);
    };
  }, [phase, secondsLeft]);

  const bestAttempt = useMemo(
    () => attempts.reduce<QuizAttempt | null>((best, a) => (!best || a.score > best.score ? a : best), null),
    [attempts]
  );

  const handleStart = async () => {
    if (!quiz || !quizId) return;
    try {
      await startQuiz(quizId);
    } catch {
    }
    setAnswers({});
    setSecondsLeft(quiz.timeLimitSeconds || null);
    setPhase('taking');
  };

  const handleSelect = (qIdx: number, optIdx: number) => {
    setAnswers((prev) => ({ ...prev, [qIdx]: optIdx }));
  };

  const handleSubmit = async () => {
    if (!quiz || !quizId) return;
    setSubmitting(true);

    const ordered = quiz.questions.map((_, i) => answers[i] ?? -1);

    try {
      const res = await submitQuiz(quizId, ordered);
      setResult(res.data);
      setAttempts((prev) => [res.data, ...prev]);
      setPhase('result');
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not submit quiz. Try again.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="quiz-page">
        <Navbar />
        <p className="quiz-loading">Loading quiz...</p>
      </div>
    );
  }

  if (!quiz) {
    return (
      <div className="quiz-page">
        <Navbar />
        <p className="quiz-loading">Quiz not found.</p>
      </div>
    );
  }

  if (phase === 'intro') {
    return (
      <div className="quiz-page">
        <Navbar />
        <div className="quiz-intro">
          <button className="back-btn" onClick={() => navigate(-1)}>
            ← Back
          </button>
          <h1 className="quiz-title">{quiz.title}</h1>
          {quiz.description && <p className="quiz-desc">{quiz.description}</p>}

          <div className="quiz-stats">
            <div>
              <p className="quiz-stat-label">Questions</p>
              <p className="quiz-stat-value">{quiz.questions.length}</p>
            </div>
            <div>
              <p className="quiz-stat-label">Passing score</p>
              <p className="quiz-stat-value">{quiz.passingScore}%</p>
            </div>
            {quiz.timeLimitSeconds ? (
              <div>
                <p className="quiz-stat-label">Time limit</p>
                <p className="quiz-stat-value">{Math.round(quiz.timeLimitSeconds / 60)} min</p>
              </div>
            ) : (
              <div>
                <p className="quiz-stat-label">Time limit</p>
                <p className="quiz-stat-value">None</p>
              </div>
            )}
          </div>

          {bestAttempt && (
            <div className="quiz-best">
              <p>
                Best so far: <b>{bestAttempt.score}%</b>{' '}
                {bestAttempt.passed ? 'passed' : 'not passed'}
              </p>
              <p className="quiz-attempt-count">{attempts.length} attempt{attempts.length === 1 ? '' : 's'}</p>
            </div>
          )}

          <button className="quiz-start-btn" onClick={handleStart}>
            Start quiz
          </button>
        </div>
      </div>
    );
  }

  if (phase === 'result' && result) {
    return (
      <div className="quiz-page">
        <Navbar />
        <div className="quiz-result">
          <div className={`quiz-result-icon ${result.passed ? 'pass' : 'fail'}`}>
            {result.passed ? '✓' : '✕'}
          </div>
          <h1>{result.passed ? 'Passed!' : 'Not passed'}</h1>
          <p className="quiz-result-score">{result.score}%</p>
          <p className="quiz-result-meta">
            Passing score for this quiz is {quiz.passingScore}%.
          </p>
          <div className="quiz-result-actions">
            <button className="back-btn" onClick={() => navigate(-1)}>
              ← Back to lesson
            </button>
            <button className="quiz-start-btn" onClick={() => setPhase('intro')}>
              Try again
            </button>
          </div>
        </div>
      </div>
    );
  }

  const allAnswered = quiz.questions.every((_, i) => answers[i] !== undefined);

  return (
    <div className="quiz-page">
      <Navbar />
      <div className="quiz-taking">
        <div className="quiz-taking-header">
          <h2 className="quiz-title-small">{quiz.title}</h2>
          {secondsLeft !== null && (
            <div className={`quiz-timer ${secondsLeft <= 30 ? 'low' : ''}`}>
              {formatTime(Math.max(0, secondsLeft))}
            </div>
          )}
        </div>

        <div className="quiz-questions">
          {quiz.questions.map((q, i) => (
            <div key={i} className="quiz-question-card">
              <p className="quiz-q-text">
                <span className="quiz-q-num">{i + 1}.</span> {q.question}
              </p>
              <div className="quiz-options">
                {q.options.map((opt, oi) => (
                  <label
                    key={oi}
                    className={`quiz-option ${answers[i] === oi ? 'selected' : ''}`}
                  >
                    <input
                      type="radio"
                      name={`q-${i}`}
                      checked={answers[i] === oi}
                      onChange={() => handleSelect(i, oi)}
                    />
                    <span>{opt}</span>
                  </label>
                ))}
              </div>
            </div>
          ))}
        </div>

        <div className="quiz-submit-row">
          <span className="quiz-progress-text">
            {Object.keys(answers).length} / {quiz.questions.length} answered
          </span>
          <button
            className="quiz-submit-btn"
            disabled={!allAnswered || submitting}
            onClick={handleSubmit}
          >
            {submitting ? 'Submitting...' : 'Submit quiz'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default QuizPage;
