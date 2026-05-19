import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getCourseById, getLessonsByCourse, Course, Lesson } from '../api/courses';
import { getCourseProgress, checkAccess, CourseProgress, getCourseRatings, rateCourse, deleteMyRating, CourseRating } from '../api';
import { isAuthenticated } from '../api/auth';
import './css/CourseLearningPage.css';

const StarSelector: React.FC<{ value: number; onChange: (v: number) => void }> = ({ value, onChange }) => {
  const [hover, setHover] = useState(0);
  return (
    <span className="clp-star-selector">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          className={`clp-star ${n <= (hover || value) ? 'clp-star-on' : ''}`}
          onMouseEnter={() => setHover(n)}
          onMouseLeave={() => setHover(0)}
          onClick={() => onChange(n)}
        >
          {n <= (hover || value) ? '★' : '☆'}
        </button>
      ))}
    </span>
  );
};

const CourseLearningPage: React.FC = () => {
  const { courseId } = useParams<{ courseId: string }>();
  const navigate = useNavigate();

  const [course, setCourse] = useState<Course | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [progress, setProgress] = useState<CourseProgress | null>(null);
  const [loading, setLoading] = useState(true);
  const [noAccess, setNoAccess] = useState(false);

  const [ratings, setRatings] = useState<CourseRating[]>([]);
  const [myRating, setMyRating] = useState<CourseRating | null>(null);
  const [formStars, setFormStars] = useState(0);
  const [formReview, setFormReview] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [ratingMsg, setRatingMsg] = useState('');

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    if (!courseId) return;
    setLoading(true);
    Promise.allSettled([
      getCourseById(courseId),
      getLessonsByCourse(courseId),
      getCourseProgress(courseId),
      checkAccess(courseId),
    ]).then(([cRes, lRes, pRes, aRes]) => {
      if (cRes.status === 'fulfilled') setCourse(cRes.value.data);
      if (lRes.status === 'fulfilled') setLessons(lRes.value.data || []);
      if (pRes.status === 'fulfilled') setProgress(pRes.value.data);
      if (aRes.status === 'fulfilled' && !aRes.value.data.hasAccess) setNoAccess(true);
      setLoading(false);
    });
  }, [courseId]);

  useEffect(() => {
    if (!courseId) return;
    getCourseRatings(courseId)
      .then((res) => setRatings(res.data || []))
      .catch(() => {});
  }, [courseId]);

  const loadRatings = () => {
    if (!courseId) return;
    getCourseRatings(courseId)
      .then((res) => setRatings(res.data || []))
      .catch(() => {});
  };

  const handleSubmitRating = async () => {
    if (!courseId || formStars === 0) {
      setRatingMsg('Please select a star rating.');
      return;
    }
    setSubmitting(true);
    setRatingMsg('');
    try {
      const res = await rateCourse(courseId, formStars, formReview.trim() || undefined);
      setMyRating(res.data);
      setRatingMsg('Rating submitted!');
      loadRatings();
    } catch (err: any) {
      setRatingMsg(err?.response?.data?.message || 'Failed to submit rating.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteRating = async () => {
    if (!courseId) return;
    setSubmitting(true);
    setRatingMsg('');
    try {
      await deleteMyRating(courseId);
      setMyRating(null);
      setFormStars(0);
      setFormReview('');
      setRatingMsg('Rating removed.');
      loadRatings();
    } catch (err: any) {
      setRatingMsg(err?.response?.data?.message || 'No rating to remove.');
    } finally {
      setSubmitting(false);
    }
  };

  const completedIds = new Set(progress?.completedLessonIds ?? []);
  const nextLesson = lessons.find((l) => !completedIds.has(l.id));
  const percent = progress?.progressPercent ?? 0;
  const doneCount = completedIds.size;

  if (loading) {
    return (
      <div className="clp-page">
        <Navbar />
        <p className="clp-loading">Loading course...</p>
      </div>
    );
  }

  if (!course) {
    return (
      <div className="clp-page">
        <Navbar />
        <p className="clp-loading">Course not found.</p>
      </div>
    );
  }

  if (noAccess) {
    return (
      <div className="clp-page">
        <Navbar />
        <div className="clp-no-access">
          <h2>Access required</h2>
          <p>You need to enroll to access this course.</p>
          <button className="clp-enroll-btn" onClick={() => navigate(`/courses/${courseId}/checkout`)}>
            Enroll now
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="clp-page">
      <Navbar />

      <div className="clp-topbar">
        <button className="back-btn" onClick={() => navigate(`/my-enrollments`)}>
          ← Back to my courses
        </button>
        <div className="clp-progress-pill">
          <span>{percent}% complete</span>
          <div className="clp-progress-bar">
            <div style={{ width: `${percent}%` }} />
          </div>
        </div>
      </div>

      <div className="clp-layout">
        <aside className="clp-sidebar">
          <h3 className="clp-sidebar-title">Course contents</h3>
          <p className="clp-sidebar-meta">{doneCount} / {lessons.length} lessons complete</p>
          <ul className="clp-sidebar-list">
            {lessons.map((l, idx) => {
              const done = completedIds.has(l.id);
              const isCurrent = nextLesson?.id === l.id;
              return (
                <li key={l.id}>
                  <Link
                    to={`/courses/${courseId}/lessons/${l.id}`}
                    className={`clp-sidebar-item${done ? ' done' : ''}${isCurrent ? ' current' : ''}`}
                  >
                    <span className="clp-ls-status">
                      {done ? '✓' : isCurrent ? '▶' : <span className="clp-ls-num">{idx + 1}</span>}
                    </span>
                    <span className="clp-ls-title">{l.title}</span>
                    {l.duration != null && <span className="clp-ls-dur">{l.duration}m</span>}
                  </Link>
                </li>
              );
            })}
          </ul>

          {progress?.completed && (
            <div className="clp-sidebar-cert">
              <button
                className="clp-sidebar-cert-btn"
                onClick={() => navigate('/certificates')}
              >
                Get Certificate
              </button>
            </div>
          )}
        </aside>

        <main className="clp-main">
          <div className="clp-course-card">
            {course.thumbnail && (
              <img className="clp-thumbnail" src={course.thumbnail} alt={course.title} />
            )}
            <div className="clp-course-info">
              <h1 className="clp-course-title">{course.title}</h1>
              <p className="clp-course-teacher">
                by{' '}
                <Link to={`/profile/${course.teacherId}`} className="author-link">
                  {course.teacherName || 'Educator'}
                </Link>
              </p>
              {course.description && (
                <p className="clp-course-desc">{course.description}</p>
              )}
              <div className="clp-course-tags">
                {course.category && <span className="clp-tag">{course.category}</span>}
                {course.level && <span className="clp-tag">{course.level}</span>}
              </div>
            </div>
          </div>

          <div className="clp-cta-card">
            <div className="clp-cta-stats">
              <div className="clp-cta-stat">
                <span className="clp-cta-stat-num">{doneCount}</span>
                <span className="clp-cta-stat-label">completed</span>
              </div>
              <div className="clp-cta-stat">
                <span className="clp-cta-stat-num">{lessons.length - doneCount}</span>
                <span className="clp-cta-stat-label">remaining</span>
              </div>
              <div className="clp-cta-stat">
                <span className="clp-cta-stat-num">{lessons.length}</span>
                <span className="clp-cta-stat-label">total</span>
              </div>
            </div>

            <div className="clp-cta-progress">
              <div className="clp-cta-progress-bar">
                <div style={{ width: `${percent}%` }} />
              </div>
              <span>{percent}%</span>
            </div>

            {progress?.completed ? (
              <div className="clp-completed-badge">
                ✓ Course completed!
                {progress.certificateId && (
                  <button className="clp-cert-btn" onClick={() => navigate('/certificates')}>
                    View certificate
                  </button>
                )}
              </div>
            ) : nextLesson ? (
              <div className="clp-cta-resume">
                <div>
                  <div className="clp-cta-label">{doneCount === 0 ? 'Start with' : 'Up next'}</div>
                  <div className="clp-cta-lesson-name">{nextLesson.title}</div>
                </div>
                <button
                  className="clp-resume-btn"
                  onClick={() => navigate(`/courses/${courseId}/lessons/${nextLesson.id}`)}
                >
                  {doneCount === 0 ? 'Start learning' : 'Resume'}
                </button>
              </div>
            ) : null}
          </div>

          <div className="clp-lessons-section">
            <h3 className="clp-lessons-title">All lessons</h3>
            <div className="clp-lessons-list">
              {lessons.map((l, idx) => {
                const done = completedIds.has(l.id);
                return (
                  <button
                    key={l.id}
                    className={`clp-lesson-row${done ? ' done' : ''}`}
                    onClick={() => navigate(`/courses/${courseId}/lessons/${l.id}`)}
                  >
                    <span className="clp-lr-index">{idx + 1}</span>
                    <span className="clp-lr-title">{l.title}</span>
                    {l.duration != null && <span className="clp-lr-dur">{l.duration} min</span>}
                    <span className="clp-lr-status">{done ? '✓' : '→'}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="clp-rating-section">
            <h3 className="clp-rating-title">
              Leave a rating
              {ratings.length > 0 && (
                <span className="clp-rating-count">{ratings.length} review{ratings.length !== 1 ? 's' : ''}</span>
              )}
            </h3>

            <div className="clp-rating-form">
              <p className="clp-rating-label">{myRating ? 'Update your rating' : 'Rate this course'}</p>
              <StarSelector value={formStars} onChange={setFormStars} />
              <textarea
                className="clp-rating-review"
                placeholder="Leave a written review (optional)…"
                rows={3}
                value={formReview}
                onChange={(e) => setFormReview(e.target.value)}
              />
              <div className="clp-rating-actions">
                <button
                  className="clp-rating-submit"
                  onClick={handleSubmitRating}
                  disabled={submitting}
                >
                  {submitting ? 'Saving…' : myRating ? 'Update' : 'Submit'}
                </button>
                {myRating && (
                  <button
                    className="clp-rating-delete"
                    onClick={handleDeleteRating}
                    disabled={submitting}
                  >
                    Remove my rating
                  </button>
                )}
              </div>
              {ratingMsg && (
                <p className={`clp-rating-msg ${ratingMsg.includes('Failed') || ratingMsg.includes('select') || ratingMsg.includes('lesson') ? 'clp-rating-msg-err' : 'clp-rating-msg-ok'}`}>
                  {ratingMsg}
                </p>
              )}
            </div>

            {ratings.length > 0 && (
              <div className="clp-rating-list">
                {ratings.map((r) => (
                  <div key={r.id} className="clp-rating-item">
                    <div className="clp-rating-item-header">
                      <div className="clp-rating-item-author">
                        {r.userAvatarUrl
                          ? <img className="clp-rating-avatar" src={r.userAvatarUrl} alt="" />
                          : <span className="clp-rating-avatar clp-rating-avatar-initials">
                              {(r.userName || '?').split(' ').map((p) => p[0]).join('').toUpperCase().slice(0, 2)}
                            </span>
                        }
                        <span className="clp-rating-item-name">{r.userName || 'Student'}</span>
                      </div>
                      <div className="clp-rating-item-meta">
                        <span className="clp-rating-item-stars">
                          {'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}
                        </span>
                        <span className="clp-rating-item-date">
                          {new Date(r.createdAt).toLocaleDateString(undefined, {
                            year: 'numeric', month: 'short', day: 'numeric',
                          })}
                        </span>
                      </div>
                    </div>
                    {r.review && <p className="clp-rating-item-review">{r.review}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
};

export default CourseLearningPage;
