import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { getMyActivity, ActivityFeed } from '../api';
import { isAuthenticated } from '../api/auth';
import './css/ActivityFeedPage.css';

const iconForType = (t: string): string => {
  switch (t) {
    case 'LESSON_COMPLETED': return '✓';
    case 'QUIZ_PASSED': return '✓';
    case 'QUIZ_FAILED': return '✗';
    case 'ENROLLMENT': return '+';
    case 'CERTIFICATE_EARNED': return '★';
    case 'COURSE_COMPLETED': return '★';
    case 'PAYMENT': return '$';
    case 'COMMENT': return '#';
    default: return '•';
  }
};

const colorForType = (t: string): string => {
  if (t.includes('PASSED') || t.includes('COMPLETED') || t.includes('EARNED')) return 'good';
  if (t.includes('FAILED')) return 'bad';
  if (t.includes('PAYMENT')) return 'gold';
  return 'neutral';
};

const formatDate = (iso: string) => {
  const d = new Date(iso);
  const diff = Date.now() - d.getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
};

const ActivityFeedPage: React.FC = () => {
  const navigate = useNavigate();
  const [items, setItems] = useState<ActivityFeed[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    setLoading(true);
    getMyActivity()
      .then((res) => setItems(res.data || []))
      .catch(() => setItems([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="activity-page">
      <Navbar />
      <div className="af-container">
        <header className="af-header">
          <h1>Recent activity</h1>
          <p className="af-sub">Your learning journey at a glance.</p>
        </header>

        {loading ? (
          <p className="af-empty">Loading...</p>
        ) : items.length === 0 ? (
          <p className="af-empty">Nothing here yet — start a course to fill this feed.</p>
        ) : (
          <ol className="af-list">
            {items.map((it) => (
              <li key={it.id} className="af-item">
                <span className={`af-icon af-${colorForType(it.type)}`}>
                  {iconForType(it.type)}
                </span>
                <div className="af-body">
                  <p className="af-msg">{it.message}</p>
                  <p className="af-time">{formatDate(it.createdAt)}</p>
                </div>
              </li>
            ))}
          </ol>
        )}
      </div>
    </div>
  );
};

export default ActivityFeedPage;
