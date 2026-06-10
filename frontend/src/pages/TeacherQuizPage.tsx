import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getTeacherQuizQuestions,
  submitTeacherQuiz,
  getTeacherQuizResult,
  TeacherQuizQuestion,
  TeacherQuizAttempt,
} from '../api';
import { getMyApplication } from '../api/profile';
import { isAuthenticated } from '../api/auth';
import './css/TeacherQuizPage.css';

const TeacherQuizPage: React.FC = () => {
  const { applicationId } = useParams<{ applicationId: string }>();
  const navigate = useNavigate();

  const [questions, setQuestions] = useState<TeacherQuizQuestion[]>([]);
  const [answers, setAnswers] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<TeacherQuizAttempt | null>(null);
  const [aiScore, setAiScore] = useState<number | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    if (!applicationId) return;
    setLoading(true);
    Promise.allSettled([
      getTeacherQuizQuestions(applicationId),
      getTeacherQuizResult(applicationId),
      getMyApplication(),
    ]).then(([qRes, rRes, aRes]) => {
      setQuestions(
        qRes.status === 'fulfilled' && qRes.value.data?.length
          ? qRes.value.data
          : []
      );
      if (rRes.status === 'fulfilled' && rRes.value.data) setResult(rRes.value.data);
      if (aRes.status === 'fulfilled' && typeof aRes.value.data?.score === 'number') {
        setAiScore(aRes.value.data.score);
      }
      setLoading(false);
    });
  }, [applicationId]);

  const handleSelect = (questionId: string, optIdx: number) => {
    setAnswers((prev) => ({ ...prev, [questionId]: optIdx }));
  };

  const handleSubmit = async () => {
    if (!applicationId) return;
    setSubmitting(true);
    try {
      const res = await submitTeacherQuiz(applicationId, answers);
      setResult(res.data);
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not submit teacher quiz.');
    } finally {
      setSubmitting(false);
    }
  };

  const allAnswered = questions.length > 0 && questions.every((q) => answers[q.id] !== undefined);

  if (loading) {
    return (
      <div className="tquiz-page">
        <Navbar />
        <p className="tquiz-loading">Loading quiz...</p>
      </div>
    );
  }

  if (result) {
    return (
      <div className="tquiz-page">
        <Navbar />
        <div className="tquiz-result">
          <div className={`tquiz-result-icon ${result.passed ? 'pass' : 'fail'}`}>
            {result.passed ? '✓' : '✕'}
          </div>
          <h1>{result.passed ? 'You passed' : 'Not passed'}</h1>
          <div className="tquiz-result-scores">
            <div className="tquiz-result-score-item">
              <span className="tquiz-result-score-label">Quiz score</span>
              <span className="tquiz-result-score">{result.score}%</span>
            </div>
            {aiScore !== null && (
              <div className="tquiz-result-score-item">
                <span className="tquiz-result-score-label">AI resume score</span>
                <span className="tquiz-result-score">{aiScore}%</span>
              </div>
            )}
          </div>
          <p className="tquiz-result-meta">
            Your application will now move to the human review stage. We'll email you with a decision.
          </p>
          <button className="tquiz-btn" onClick={() => navigate('/profile')}>
            Back to profile
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="tquiz-page">
      <Navbar />
      <div className="tquiz-container">
        <button className="back-btn" onClick={() => navigate(-1)}>
          ← Back
        </button>

        <header className="tquiz-header">
          <h1>Teacher qualification quiz</h1>
          <p className="tquiz-sub">
            Answer all questions to complete your teacher application. You can only take this quiz once.
          </p>
        </header>

        <div className="tquiz-questions">
          {questions.map((q, i) => (
            <div key={q.id} className="tquiz-card">
              <p className="tquiz-q">
                <span className="tquiz-num">{i + 1}.</span> {q.question}
              </p>
              <div className="tquiz-options">
                {q.options.map((opt, oi) => (
                  <label
                    key={oi}
                    className={`tquiz-option ${answers[q.id] === oi ? 'selected' : ''}`}
                  >
                    <input
                      type="radio"
                      name={`tq-${q.id}`}
                      checked={answers[q.id] === oi}
                      onChange={() => handleSelect(q.id, oi)}
                    />
                    <span>{opt}</span>
                  </label>
                ))}
              </div>
            </div>
          ))}
        </div>

        <div className="tquiz-submit-row">
          <span className="tquiz-progress">
            {Object.keys(answers).length} / {questions.length} answered
          </span>
          <button
            className="tquiz-btn"
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

export default TeacherQuizPage;
