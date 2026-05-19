import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getMyBookmarks, toggleBookmark, BookmarkCourse } from '../api';
import { isAuthenticated } from '../api/auth';
import './css/MyBookmarksPage.css';

const MyBookmarksPage: React.FC = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<BookmarkCourse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    setLoading(true);
    getMyBookmarks()
      .then((res) => setItems(res.data || []))
      .catch(() => setItems([]))
      .finally(() => setLoading(false));
  }, []);

  const handleRemove = async (courseId: string) => {
    try {
      await toggleBookmark(courseId);
    } catch {
    }
    setItems((prev) => prev.filter((c) => c.id !== courseId));
  };

  return (
    <div className="bookmarks-page">
      <Navbar />
      <div className="bm-container">
        <header className="bm-header">
          <h1>Saved courses</h1>
          <p className="bm-sub">{items.length} bookmarked</p>
        </header>

        {loading ? (
          <p className="bm-empty">Loading bookmarks...</p>
        ) : items.length === 0 ? (
          <div className="bm-empty-card">
            <p className="bm-empty">You haven't saved any courses yet.</p>
            <Link to="/courses" className="bm-browse-btn">
              Browse courses
            </Link>
          </div>
        ) : (
          <div className="bm-grid">
            {items.map((c) => (
              <div key={c.id} className="bm-card">
                <Link to={`/courses/${c.id}`} className="bm-thumb">
                  {c.thumbnail ? (
                    <img src={c.thumbnail} alt={c.title} />
                  ) : (
                    <span className="bm-thumb-ph">{c.title?.[0] || '?'}</span>
                  )}
                </Link>
                <div className="bm-info">
                  <Link to={`/courses/${c.id}`} className="bm-title">
                    {c.title}
                  </Link>
                  <p className="bm-teacher">{c.teacherName || 'Educator'}</p>
                  <div className="bm-meta">
                    {c.rating !== undefined && (
                      <span className="bm-rating">★ {c.rating.toFixed(1)}</span>
                    )}
                    <span className="bm-price">{c.price ? `$${c.price}` : 'Free'}</span>
                  </div>
                  <button
                    className="bm-remove-btn"
                    onClick={() => handleRemove(c.id)}
                  >
                    Remove bookmark
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyBookmarksPage;
