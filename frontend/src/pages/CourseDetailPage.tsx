import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getCourseById, getLessonsByCourse, Course, Lesson } from '../api/courses';
import { getCourseRatings, CourseRating } from '../api';
import { isAuthenticated } from '../api/auth';
import './css/CourseDetailPage.css';

const StarRating: React.FC<{ rating: number }> = ({ rating }) => (
  <span className="detail-stars">
    {'★'.repeat(Math.floor(rating))}{'☆'.repeat(5 - Math.floor(rating))}
    <span className="detail-rating-num"> {rating.toFixed(1)}</span>
  </span>
);

const CourseDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [course, setCourse] = useState<Course | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [ratings, setRatings] = useState<CourseRating[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      getCourseById(id).catch(() => ({ data: null })),
      getLessonsByCourse(id).catch(() => ({ data: [] as Lesson[] })),
      getCourseRatings(id).catch(() => ({ data: [] as CourseRating[] })),
    ]).then(([cRes, lRes, rRes]) => {
      setCourse(cRes.data);
      setLessons(lRes.data || []);
      setRatings(rRes.data || []);
    }).finally(() => setLoading(false));
  }, [id]);

  const handleEnroll = () => {
    if (!isAuthenticated()) {
      navigate('/login');
      return;
    }
    navigate(`/courses/${id}/checkout`);
  };

  if (loading) {
    return (
      <div className="detail-page">
        <Navbar />
        <div className="detail-loading">Loading course...</div>
      </div>
    );
  }

  if (!course) {
    return (
      <div className="detail-page">
        <Navbar />
        <div className="detail-loading">Course not found.</div>
      </div>
    );
  }

  const isFree = course.free === true;
  const displayRating = course.avgRating ?? course.rating;

  return (
    <div className="detail-page">
      <Navbar />

      <div className="detail-topbar">
        <button className="back-btn" onClick={() => navigate('/courses')}>
          {'←'} Back
        </button>
      </div>

      <div className="detail-content">

        <div className="detail-left">
          <div className="detail-media">
            {course.thumbnail
              ? <img src={course.thumbnail} alt={course.title} className="detail-media-img" />
              : <span className="detail-media-placeholder">Introduction Picture or Video</span>
            }
          </div>

          <div className="detail-meta-row">
            <Link to={`/profile/${course.teacherId}`} className="detail-educator author-link">
              {course.teacherName || 'Educator Name'}
            </Link>
            <span className="detail-rate">
              {displayRating ? <StarRating rating={displayRating} /> : 'No rating'}
            </span>
          </div>

          <div className="detail-tags-row">
            {course.category && <span className="detail-tag">{course.category}</span>}
            {course.level && <span className="detail-tag">{course.level}</span>}
          </div>

          <div className="detail-lessons-box">
            <p className="detail-lessons-header">Lessons</p>
            {lessons.length > 0 ? (
              <ul className="detail-lessons-list">
                {lessons.map((lesson, idx) => (
                  <li key={lesson.id} className="detail-lesson-item">
                    <span className="lesson-index">{idx + 1}.</span>
                    <span className="lesson-title">{lesson.title}</span>
                    {lesson.duration && (
                      <span className="lesson-duration">{lesson.duration} min</span>
                    )}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="detail-lessons-empty">No lessons published yet.</p>
            )}
          </div>
        </div>

        <div className="detail-right">
          <div className="detail-about-card">
            <div className="detail-title-row">
              <h1 className="detail-topic">{course.title}</h1>
              <span className={`detail-access-badge ${isFree ? 'free' : 'subscription'}`}>
                {isFree ? 'Free' : 'Subscription'}
              </span>
            </div>
            <p className="detail-about-text">{course.description}</p>
            <button className="detail-enroll-btn" onClick={handleEnroll}>
              Enroll Now
            </button>
          </div>
        </div>

      </div>

      {/* Read-only ratings list */}
      <div className="detail-ratings">
        <div className="dr-inner">
          <h2 className="dr-heading">
            Ratings
            {ratings.length > 0 && (
              <span className="dr-count">{ratings.length} review{ratings.length !== 1 ? 's' : ''}</span>
            )}
          </h2>

          {ratings.length === 0 ? (
            <p className="dr-empty">No reviews yet.</p>
          ) : (
            <div className="dr-list">
              {ratings.map((r) => (
                <div key={r.id} className="dr-item">
                  <div className="dr-item-header">
                    <div className="dr-item-author">
                      {r.userAvatarUrl
                        ? <img className="dr-avatar" src={r.userAvatarUrl} alt="" />
                        : <span className="dr-avatar dr-avatar-initials">
                            {(r.userName || '?').split(' ').map((p) => p[0]).join('').toUpperCase().slice(0, 2)}
                          </span>
                      }
                      <span className="dr-item-name">{r.userName || 'Student'}</span>
                    </div>
                    <div className="dr-item-meta">
                      <span className="dr-item-stars">
                        {'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}
                      </span>
                      <span className="dr-item-date">
                        {new Date(r.createdAt).toLocaleDateString(undefined, {
                          year: 'numeric', month: 'short', day: 'numeric',
                        })}
                      </span>
                    </div>
                  </div>
                  {r.review && <p className="dr-item-review">{r.review}</p>}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CourseDetailPage;
