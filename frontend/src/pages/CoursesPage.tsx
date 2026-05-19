import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getCourses, Course } from '../api/courses';
import './css/CoursesPage.css';

const LEVELS = ['All', 'Beginner', 'Intermediate', 'Advanced'];
const CATEGORIES = ['All', 'Programming', 'Mathematics', 'Physics', 'Sciences'];
const RATING_OPTIONS = ['All', '4.5+', '4.0+', '3.5+'];
const ACCESS_OPTIONS = ['All', 'Free', 'Subscription'];


const StarRating: React.FC<{ rating: number }> = ({ rating }) => {
  return (
    <span className="star-rating">
      {'★'.repeat(Math.floor(rating))}{'☆'.repeat(5 - Math.floor(rating))}
      <span className="rating-num"> {rating.toFixed(1)}</span>
    </span>
  );
};

const CoursesPage: React.FC = () => {
  const navigate = useNavigate();
  const [courses, setCourses] = useState<Course[]>([]);
  const [search, setSearch] = useState('');
  const [showSearch, setShowSearch] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [levelFilter, setLevelFilter] = useState('All');
  const [categoryFilter, setCategoryFilter] = useState('All');
  const [ratingFilter, setRatingFilter] = useState('All');
  const [accessFilter, setAccessFilter] = useState('All');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    getCourses({
      category: categoryFilter !== 'All' ? categoryFilter : undefined,
      level: levelFilter !== 'All' ? levelFilter : undefined,
    })
      .then(res => setCourses(res.data?.content ?? []))
      .catch(() => setCourses([]))
      .finally(() => setLoading(false));
  }, [levelFilter, categoryFilter]);

  const filtered = courses.filter(c => {
    const matchSearch = c.title.toLowerCase().includes(search.toLowerCase()) ||
      (c.teacherName || '').toLowerCase().includes(search.toLowerCase());
    const rating = c.avgRating ?? c.rating ?? 0;
    const minRating = ratingFilter === '4.5+' ? 4.5 : ratingFilter === '4.0+' ? 4.0 : ratingFilter === '3.5+' ? 3.5 : 0;
    const matchRating = rating >= minRating;
    const isFree = c.free === true;
    const matchAccess = accessFilter === 'All' || (accessFilter === 'Free' ? isFree : !isFree);
    return matchSearch && matchRating && matchAccess;
  });

  return (
    <div className="courses-page">
      <Navbar />

      <div className="courses-toolbar">
        <div className="toolbar-right">
          <div className="search-area">
            {showSearch && (
              <input
                className="search-input"
                type="text"
                placeholder="Search courses or educators..."
                value={search}
                onChange={e => setSearch(e.target.value)}
                autoFocus
              />
            )}
            <button
              className="toolbar-btn search-btn"
              onClick={() => { setShowSearch(s => !s); if (showSearch) setSearch(''); }}
            >
              <span className="toolbar-label">Search</span>
            </button>
          </div>
          <div className="filter-btn-wrap">
            <button
              className={`toolbar-btn ${showFilters ? 'active' : ''}`}
              onClick={() => setShowFilters(s => !s)}
            >
              <span className="toolbar-label">Filters</span>
            </button>
            {showFilters && (
              <div className="filter-panel">
                <div className="filter-group">
                  <span className="filter-group-label">Level</span>
                  <div className="filter-chips">
                    {LEVELS.map(l => (
                      <button key={l} className={`filter-chip ${levelFilter === l ? 'active' : ''}`} onClick={() => setLevelFilter(l)}>
                        {l}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="filter-group">
                  <span className="filter-group-label">Category</span>
                  <div className="filter-chips">
                    {CATEGORIES.map(c => (
                      <button key={c} className={`filter-chip ${categoryFilter === c ? 'active' : ''}`} onClick={() => setCategoryFilter(c)}>
                        {c}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="filter-group">
                  <span className="filter-group-label">Rating</span>
                  <div className="filter-chips">
                    {RATING_OPTIONS.map(r => (
                      <button key={r} className={`filter-chip ${ratingFilter === r ? 'active' : ''}`} onClick={() => setRatingFilter(r)}>
                        {r === 'All' ? 'All' : <><span className="chip-star"></span>{r}</>}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="filter-group">
                  <span className="filter-group-label">Access</span>
                  <div className="filter-chips">
                    {ACCESS_OPTIONS.map(a => (
                      <button key={a} className={`filter-chip ${accessFilter === a ? 'active' : ''}`} onClick={() => setAccessFilter(a)}>
                        {a}
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Courses grid */}
      <div className="courses-body">
        <div className="courses-container">
        {loading ? (
          <div className="courses-loading">Loading courses...</div>
        ) : filtered.length === 0 ? (
          <div className="courses-empty">No published courses found.</div>
        ) : (
          <div className="courses-grid">
            {filtered.map(course => (
              <Link to={`/courses/${course.id}`} key={course.id} className="course-card">
                <p className="course-topic">{course.title}</p>
                <div className="course-thumbnail">
                  {course.thumbnail
                    ? <img src={course.thumbnail.startsWith('http') ? course.thumbnail : `http://localhost:8080/files?path=${course.thumbnail}`} alt={course.title} />
                    : <span className="thumb-placeholder">No thumbnail</span>
                  }
                </div>
                {course.category && (
                  <span className="course-category">{course.category}</span>
                )}
                <p className="course-educator">
                  by{' '}
                  <span
                    className="author-link"
                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); navigate(`/profile/${course.teacherId}`); }}
                  >
                    {course.teacherName || 'Unknown'}
                  </span>
                </p>
                {course.lessonCount !== undefined && (
                  <p className="course-lessons">{course.lessonCount} {course.lessonCount === 1 ? 'lesson' : 'lessons'}</p>
                )}
                <div className="course-footer">
                  <span className="course-rate">
                    {(course.avgRating || course.rating)
                      ? <StarRating rating={(course.avgRating || course.rating)!} />
                      : 'No rating'}
                  </span>
                  <span className="course-btn-enroll">
                    {course.free ? 'Free' : 'Subscription'}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
        </div>
      </div>
    </div>
  );
};

export default CoursesPage;
