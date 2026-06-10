import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getMyEnrollments,
  getCourseProgress,
  confirmSubscription,
  getMySubscriptions,
  Enrollment,
  CourseProgress,
} from '../api';
import { getCourseById, getCourses, Course } from '../api/courses';
import { isAuthenticated } from '../api/auth';
import './css/MyEnrollmentsPage.css';

interface EnrollmentRow {
  enrollment: Enrollment;
  course: Course | null;
  progress: CourseProgress | null;
}

const MyEnrollmentsPage: React.FC = () => {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();

  const [rows, setRows] = useState<EnrollmentRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'in-progress' | 'completed'>('all');
  const [subStatus, setSubStatus] = useState<'idle' | 'confirming' | 'success' | 'error'>('idle');
  const [subError, setSubError] = useState('');

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    const subscriptionId = params.get('subscription_id');
    if (!subscriptionId) return;
    setSubStatus('confirming');
    confirmSubscription(subscriptionId)
      .then(() => setSubStatus('success'))
      .catch((err: any) => {
        setSubError(err?.response?.data?.message || 'Could not confirm subscription.');
        setSubStatus('error');
      })
      .finally(() => {
        params.delete('subscription_id');
        params.delete('token');
        setParams(params, { replace: true });
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      try {
        const enrRes = await getMyEnrollments();
        const enrollments = enrRes.data || [];
        const expanded = await Promise.all(
          enrollments.map(async (e): Promise<EnrollmentRow> => {
            const [cRes, pRes] = await Promise.allSettled([
              getCourseById(e.courseId),
              getCourseProgress(e.courseId),
            ]);
            return {
              enrollment: e,
              course: cRes.status === 'fulfilled' ? cRes.value.data : null,
              progress: pRes.status === 'fulfilled' ? pRes.value.data : null,
            };
          })
        );

        // PRO subscribers get access to every subscription-required course
        // without an Enrollment record - list those here too.
        const subRes = await getMySubscriptions().catch(() => null);
        const hasActiveSubscription = (subRes?.data || []).some((s) => s.status === 'ACTIVE');

        let proRows: EnrollmentRow[] = [];
        if (hasActiveSubscription) {
          const enrolledCourseIds = new Set(enrollments.map((e) => e.courseId));
          const coursesRes = await getCourses({ size: 100 }).catch(() => null);
          const proCourses = (coursesRes?.data.content || []).filter(
            (c) => c.free !== true && !enrolledCourseIds.has(c.id)
          );
          proRows = await Promise.all(
            proCourses.map(async (c): Promise<EnrollmentRow> => {
              const pRes = await getCourseProgress(c.id).catch(() => null);
              return {
                enrollment: {
                  id: `sub-${c.id}`,
                  userId: '',
                  courseId: c.id,
                  status: 'ACTIVE',
                  enrolledAt: '',
                },
                course: c,
                progress: pRes?.data || null,
              };
            })
          );
        }

        setRows([...expanded, ...proRows]);
      } catch {
        setRows([]);
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, [subStatus]);

  const filtered = rows.filter((r) => {
    if (filter === 'in-progress') return !r.progress?.completed;
    if (filter === 'completed') return r.progress?.completed;
    return true;
  });

  const totalCount = rows.length;
  const completedCount = rows.filter((r) => r.progress?.completed).length;

  return (
    <div className="enrollments-page">
      <Navbar />

      <div className="enr-container">
        {subStatus === 'confirming' && (
          <div className="enr-empty-card">
            <p className="enr-empty">Confirming your subscription...</p>
          </div>
        )}
        {subStatus === 'success' && (
          <div className="enr-empty-card">
            <p className="enr-empty">✓ Subscription activated! You now have full access to PRO courses.</p>
          </div>
        )}
        {subStatus === 'error' && (
          <div className="enr-empty-card">
            <p className="enr-empty">{subError}</p>
          </div>
        )}

        <header className="enr-header">
          <h1>My learning</h1>
          <p className="enr-sub">
            {totalCount} course{totalCount === 1 ? '' : 's'} · {completedCount} completed
          </p>
        </header>

        <div className="enr-filters">
          <button
            className={`enr-filter ${filter === 'all' ? 'active' : ''}`}
            onClick={() => setFilter('all')}
          >
            All
          </button>
          <button
            className={`enr-filter ${filter === 'in-progress' ? 'active' : ''}`}
            onClick={() => setFilter('in-progress')}
          >
            In progress
          </button>
          <button
            className={`enr-filter ${filter === 'completed' ? 'active' : ''}`}
            onClick={() => setFilter('completed')}
          >
            Completed
          </button>
        </div>

        {loading ? (
          <p className="enr-empty">Loading your courses...</p>
        ) : filtered.length === 0 ? (
          <div className="enr-empty-card">
            <p className="enr-empty">
              {filter === 'all'
                ? "You haven't enrolled in anything yet."
                : `No ${filter.replace('-', ' ')} courses.`}
            </p>
            <Link to="/courses" className="enr-browse-btn">
              Browse courses
            </Link>
          </div>
        ) : (
          <div className="enr-grid">
            {filtered.map((r) => {
              const c = r.course;
              const pct = r.progress?.progressPercent ?? 0;
              const done = r.progress?.completed;
              return (
                <div
                  key={r.enrollment.id}
                  className="enr-card"
                  onClick={() => navigate(`/courses/${r.enrollment.courseId}/learn`)}
                  style={{ cursor: 'pointer' }}
                >
                  <div className="enr-thumb">
                    {c?.thumbnail ? (
                      <img src={c.thumbnail} alt={c.title} />
                    ) : (
                      <span className="enr-thumb-ph">{c?.title?.[0] || '?'}</span>
                    )}
                    {done && <span className="enr-completed-badge">✓ Completed</span>}
                  </div>
                  <div className="enr-info">
                    <p className="enr-card-title">{c?.title || 'Course'}</p>
                    <Link
                      to={`/profile/${c?.teacherId}`}
                      className="enr-card-teacher author-link"
                      onClick={(e) => e.stopPropagation()}
                    >
                      {c?.teacherName || 'Educator'}
                    </Link>
                    <div className="enr-progress">
                      <div className="enr-progress-bar">
                        <div style={{ width: `${pct}%` }} />
                      </div>
                      <span className="enr-progress-pct">{pct}%</span>
                    </div>
                    {done && r.progress?.certificateId && (
                      <Link
                        to={`/certificates/${r.progress.certificateId}`}
                        className="enr-cert-link"
                        onClick={(e) => e.stopPropagation()}
                      >
                        View certificate →
                      </Link>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyEnrollmentsPage;
