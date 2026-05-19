import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getAdminStats,
  getAdminUsers,
  deleteAdminUser,
  getPendingApplications,
  approveApplication,
  rejectApplication,
  AdminStats,
  AdminUser,
  TeacherApplicationDetail,
} from '../api';
import { isAuthenticated, getUserRole } from '../api/auth';
import './css/AdminDashboardPage.css';

const isAiFallback = (app: TeacherApplicationDetail) =>
  app.aiSummary?.includes('AI service unavailable') ?? false;

const formatDate = (raw: string | number[]) => {
  if (Array.isArray(raw)) {
    const [y, m, d] = raw as number[];
    return new Date(y, m - 1, d).toLocaleDateString(undefined, {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  }
  const d = new Date(raw);
  return isNaN(d.getTime()) ? String(raw) : d.toLocaleDateString(undefined, {
    year: 'numeric', month: 'short', day: 'numeric',
  });
};

const formatMoney = (n: number) =>
  n.toLocaleString(undefined, { style: 'currency', currency: 'USD' });

const AdminDashboardPage: React.FC = () => {
  const navigate = useNavigate();

  const [tab, setTab] = useState<'overview' | 'users' | 'applications'>('overview');
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [apps, setApps] = useState<TeacherApplicationDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [reviewModal, setReviewModal] = useState<TeacherApplicationDetail | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  if (isAuthenticated() && getUserRole() !== 'ADMIN') {
    return (
      <div className="admin-page">
        <div className="adm-container">
          <p className="adm-empty" style={{ color: 'var(--danger, #e53e3e)' }}>
            Access denied. You do not have permission to view this page.
          </p>
        </div>
      </div>
    );
  }

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [sRes, uRes, aRes] = await Promise.all([
          getAdminStats(),
          getAdminUsers(),
          getPendingApplications(),
        ]);
        setStats(sRes.data);
        setUsers(uRes.data);
        setApps(aRes.data);
      } catch (err: any) {
        setError(err?.response?.data?.message || 'Failed to load dashboard data.');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleDeleteUser = async (id: string, name: string) => {
    if (!window.confirm(`Permanently delete ${name}?`)) return;
    try {
      await deleteAdminUser(id);
      setUsers((prev) => prev.filter((u) => u.id !== id));
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not delete user.');
    }
  };

  const handleApprove = async (appId: string, comment?: string) => {
    try {
      await approveApplication(appId, comment);
      setApps((prev) => prev.filter((a) => a.id !== appId));
      setReviewModal(null);
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not approve application.');
    }
  };

  const handleReject = async (appId: string, comment?: string) => {
    try {
      await rejectApplication(appId, comment);
      setApps((prev) => prev.filter((a) => a.id !== appId));
      setReviewModal(null);
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not reject application.');
    }
  };

  const filteredUsers = users.filter(
    (u) =>
      u.name.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="admin-page">
      <div className="adm-container">
        <header className="adm-header">
          <h1>Admin dashboard</h1>
          </header>

        <div className="adm-tabs">
          <button
            className={`adm-tab ${tab === 'overview' ? 'active' : ''}`}
            onClick={() => setTab('overview')}
          >
            Overview
          </button>
          <button
            className={`adm-tab ${tab === 'users' ? 'active' : ''}`}
            onClick={() => setTab('users')}
          >
            Users ({users.length})
          </button>
          <button
            className={`adm-tab ${tab === 'applications' ? 'active' : ''}`}
            onClick={() => setTab('applications')}
          >
            Applications ({apps.length})
          </button>
        </div>

        {loading ? (
          <p className="adm-empty">Loading...</p>
        ) : error ? (
          <p className="adm-empty" style={{ color: 'var(--danger, #e53e3e)' }}>{error}</p>
        ) : tab === 'overview' ? (
          stats ? (
            <div className="adm-stats-grid">
              <div className="adm-stat-card">
                <p className="adm-stat-label">Total users</p>
                <p className="adm-stat-value">{stats.totalUsers.toLocaleString()}</p>
                <p className="adm-stat-foot">
                  {stats.totalStudents} students · {stats.totalTeachers} teachers
                </p>
              </div>
              <div className="adm-stat-card">
                <p className="adm-stat-label">Courses</p>
                <p className="adm-stat-value">{stats.totalCourses.toLocaleString()}</p>
                <p className="adm-stat-foot">Published</p>
              </div>
            </div>
          ) : (
            <p className="adm-empty">No stats available.</p>
          )
        ) : tab === 'users' ? (
          <>
            <input
              className="adm-search"
              type="text"
              placeholder="Search users by name or email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            <div className="adm-table">
              <div className="adm-table-head">
                <span>Name</span>
                <span>Email</span>
                <span>Role</span>
                <span>Status</span>
                <span>Joined</span>
                <span></span>
              </div>
              {filteredUsers.length === 0 ? (
                <p className="adm-empty">No users match.</p>
              ) : (
                filteredUsers.map((u) => (
                  <div key={u.id} className="adm-table-row">
                    <span className="adm-name">{u.name}</span>
                    <span className="adm-email">{u.email}</span>
                    <span>
                      <span className={`adm-role role-${u.role.toLowerCase()}`}>{u.role}</span>
                    </span>
                    <span className={u.enabled ? 'adm-on' : 'adm-off'}>
                      {u.enabled ? '● Active' : '○ Disabled'}
                    </span>
                    <span className="adm-mono">{formatDate(u.createdAt as any)}</span>
                    <span>
                      {u.role !== 'ADMIN' && (
                        <button
                          className="adm-danger-btn"
                          onClick={() => handleDeleteUser(u.id, u.name)}
                        >
                          Delete
                        </button>
                      )}
                    </span>
                  </div>
                ))
              )}
            </div>
          </>
        ) : (
          <div className="adm-apps-list">
            {apps.length === 0 ? (
              <p className="adm-empty">No pending applications.</p>
            ) : (
              apps.map((a) => (
                <div key={a.id} className="adm-app-card">
                  <div className="adm-app-head">
                    <div>
                      <h3>{a.fullName}</h3>
                      <p className="adm-app-meta">
                        {a.email} · {a.specialization} · {a.yearsOfExperience}yr
                      </p>
                    </div>
                    {a.score !== undefined && (
                      <div className={`adm-app-score ${a.score >= 70 ? 'score-high' : a.score >= 50 ? 'score-mid' : 'score-low'}`}>
                        <span>{a.score}</span>
                        <small>{isAiFallback(a) ? 'fallback' : 'AI score'}</small>
                      </div>
                    )}
                  </div>
                  {a.aiSummary && <p className="adm-app-summary">{a.aiSummary}</p>}
                  <div className="adm-app-actions">
                    <button
                      className="adm-secondary-btn"
                      onClick={() => setReviewModal(a)}
                    >
                      Review
                    </button>
                    <button
                      className="adm-approve-btn"
                      onClick={() => handleApprove(a.id)}
                    >
                      Approve
                    </button>
                    <button
                      className="adm-danger-btn"
                      onClick={() => handleReject(a.id)}
                    >
                      Reject
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      {reviewModal && (
        <ReviewModal
          app={reviewModal}
          onClose={() => setReviewModal(null)}
          onApprove={(c) => handleApprove(reviewModal.id, c)}
          onReject={(c) => handleReject(reviewModal.id, c)}
        />
      )}
    </div>
  );
};

interface ReviewModalProps {
  app: TeacherApplicationDetail;
  onClose: () => void;
  onApprove: (comment?: string) => void;
  onReject: (comment?: string) => void;
}

const ReviewModal: React.FC<ReviewModalProps> = ({ app, onClose, onApprove, onReject }) => {
  const [comment, setComment] = useState('');
  return (
    <div className="adm-modal-bg" onClick={onClose}>
      <div className="adm-modal" onClick={(e) => e.stopPropagation()}>
        <h2>{app.fullName}</h2>
        <p className="adm-app-meta">
          {app.email} · {app.specialization} · {app.yearsOfExperience}yr
        </p>

        {app.score !== undefined && (
          <div className="adm-modal-score">
            <div className="adm-modal-score-header">
              <p>AI screening score {isAiFallback(app) && <span className="adm-fallback-tag">fallback</span>}</p>
              <h3>{app.score} / 100</h3>
            </div>
            <div className="adm-score-bar-track">
              <div
                className={`adm-score-bar-fill ${app.score >= 70 ? 'score-high' : app.score >= 50 ? 'score-mid' : 'score-low'}`}
                style={{ width: `${app.score}%` }}
              />
              <div className="adm-score-bar-threshold" title="Badge threshold (70)" />
            </div>
            <p className="adm-score-hint">
              {app.score >= 70 ? 'Recommended for badge issuance' : app.score >= 50 ? 'Borderline — review carefully' : 'Below threshold — consider rejecting'}
            </p>
          </div>
        )}

        {app.resumeUrl && (
          <a
            className="adm-resume-link"
            href={app.resumeUrl}
            target="_blank"
            rel="noopener noreferrer"
          >
            View Resume PDF
          </a>
        )}

        {app.aiSummary && (
          <section>
            <h4>Summary</h4>
            <p>{app.aiSummary}</p>
          </section>
        )}
        {app.aiStrengths && (
          <section>
            <h4>Strengths</h4>
            <p>{app.aiStrengths}</p>
          </section>
        )}
        {app.aiWeaknesses && (
          <section>
            <h4>Weaknesses</h4>
            <p>{app.aiWeaknesses}</p>
          </section>
        )}
        {app.aiRecommendation && (
          <section>
            <h4>Recommendation</h4>
            <p>{app.aiRecommendation}</p>
          </section>
        )}

        <textarea
          className="adm-modal-comment"
          rows={3}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Optional review comment..."
        />

        <div className="adm-modal-actions">
          <button className="adm-secondary-btn" onClick={onClose}>
            Cancel
          </button>
          <button className="adm-danger-btn" onClick={() => onReject(comment.trim() || undefined)}>
            Reject
          </button>
          <button className="adm-approve-btn" onClick={() => onApprove(comment.trim() || undefined)}>
            Approve &amp; Issue Badge
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardPage;
