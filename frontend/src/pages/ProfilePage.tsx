import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { Course } from '../api/courses';
import {
  getMyCourses,
  getMyEnrollments,
  getCourseProgress,
  Enrollment,
  CourseProgress,
} from '../api';
import { getCourseById } from '../api/courses';
import { getProfile, getMyReviews, getPublicProfile, updateProfile, UserProfile, Review } from '../api/profile';
import { isAuthenticated, getUserRole } from '../api/auth';
import './css/ProfilePage.css';

interface EnrollmentRow {
  enrollment: Enrollment;
  course: Course | null;
  progress: CourseProgress | null;
}

const StarRating: React.FC<{ rating: number; size?: 'sm' | 'md' }> = ({
  rating,
  size = 'sm',
}) => (
  <span className="review-stars" style={{ fontSize: size === 'md' ? '1rem' : '0.8rem' }}>
    {'★'.repeat(Math.floor(rating))}
    {'☆'.repeat(5 - Math.floor(rating))}
  </span>
);

const ReviewCard: React.FC<{ review: Review }> = ({ review }) => {
  const initials = (review.userName ?? '')
    .split(' ')
    .map((w) => w[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  return (
    <div className="review-card">
      <div className="review-header">
        <div className="review-avatar">{initials}</div>
        <div className="review-meta">
          <span className="review-reviewer-name">{review.userName}</span>
          <StarRating rating={review.rating} />
        </div>
      </div>
      <p className="review-comment">{review.review}</p>
    </div>
  );
};

interface EditModalProps {
  profile: UserProfile;
  onClose: () => void;
  onSave: (updated: Partial<UserProfile>) => Promise<void>;
}

const EditProfileModal: React.FC<EditModalProps> = ({ profile, onClose, onSave }) => {
  const navigate = useNavigate();
  const isStudent = getUserRole() === 'STUDENT';
  const [name, setName] = useState(profile.name || '');
  const [bio, setBio] = useState(profile.bio || '');
  const [twitter, setTwitter] = useState(profile.socialLinks?.twitter || '');
  const [linkedin, setLinkedin] = useState(profile.socialLinks?.linkedin || '');
  const [github, setGithub] = useState(profile.socialLinks?.github || '');
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    const updated: Partial<UserProfile> = { name };
    if (!isStudent) {
      updated.bio = bio;
      updated.socialLinks = { twitter, linkedin, github };
    }
    await onSave(updated);
    setSaving(false);
    onClose();
  };

  return (
    <div className="profile-modal-bg" onClick={onClose}>
      <div className="profile-modal" onClick={(e) => e.stopPropagation()}>
        <h2 className="modal-title">Edit Profile</h2>

        <div>
          <label className="modal-label">Name</label>
          <input
            className="modal-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Your name"
          />
        </div>

        {!isStudent && (
          <>
            <div>
              <label className="modal-label">Short Introduction</label>
              <textarea
                className="modal-textarea"
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                placeholder="Tell others about yourself..."
              />
            </div>

            <div>
              <label className="modal-label">Twitter URL</label>
              <input
                className="modal-input"
                value={twitter}
                onChange={(e) => setTwitter(e.target.value)}
                placeholder="https://twitter.com/you"
              />
            </div>

            <div>
              <label className="modal-label">LinkedIn URL</label>
              <input
                className="modal-input"
                value={linkedin}
                onChange={(e) => setLinkedin(e.target.value)}
                placeholder="https://linkedin.com/in/you"
              />
            </div>

            <div>
              <label className="modal-label">GitHub URL</label>
              <input
                className="modal-input"
                value={github}
                onChange={(e) => setGithub(e.target.value)}
                placeholder="https://github.com/you"
              />
            </div>
          </>
        )}

        {!(['teacher', 'educator', 'TEACHER', 'EDUCATOR'].includes(getUserRole() ?? '')) && (
          <div className="modal-teacher-apply">
            <div className="modal-teacher-apply-info">
              <span className="modal-teacher-check">✓</span>
              <div>
                <p className="modal-teacher-title">Become a Qualified Teacher</p>
                <p className="modal-teacher-sub">Submit your resume for AI screening & admin review</p>
              </div>
            </div>
            <button
              className="modal-teacher-btn"
              onClick={() => { onClose(); navigate('/teacher-apply'); }}
            >
              Apply →
            </button>
          </div>
        )}

        <div className="modal-actions">
          <button className="modal-btn-cancel" onClick={onClose}>
            Cancel
          </button>
          <button className="modal-btn-save" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>
    </div>
  );
};

const ProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { userId } = useParams<{ userId?: string }>();
  const isViewMode = !!userId;

  const role = getUserRole() ?? '';
  const isStudent = role === 'STUDENT';
  const isTeacher = ['TEACHER', 'EDUCATOR', 'teacher', 'educator'].includes(role);

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [enrolledRows, setEnrolledRows] = useState<EnrollmentRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);

  const [search, setSearch] = useState('');
  const [showSearch, setShowSearch] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [levelFilter, setLevelFilter] = useState('All');
  const [categoryFilter, setCategoryFilter] = useState('All');
  const [ratingFilter, setRatingFilter] = useState('All');

  const [enrollSearch, setEnrollSearch] = useState('');
  const [showEnrollSearch, setShowEnrollSearch] = useState(false);
  const [showEnrollFilters, setShowEnrollFilters] = useState(false);
  const [enrollLevelFilter, setEnrollLevelFilter] = useState('All');
  const [enrollCategoryFilter, setEnrollCategoryFilter] = useState('All');

  useEffect(() => {
    if (!isViewMode && !isAuthenticated()) {
      navigate('/login', { replace: true });
    }
  }, [navigate, isViewMode]);

  useEffect(() => {
    const fetchAll = async () => {
      setLoading(true);
      try {
        if (isViewMode && userId) {
          const res = await getPublicProfile(userId);
          const pub = res.data;
          setProfile({
            id: pub.id,
            name: pub.name,
            email: '',
            role: pub.role,
            avatarUrl: pub.profileImageUrl,
            bio: pub.bio,
            socialLinks: pub.socialLinks,
          });
          setCourses(pub.courses);
          setReviews(pub.reviews ?? []);
          return;
        }

        if (isStudent) {
          const [profileRes, enrRes] = await Promise.allSettled([
            getProfile(),
            getMyEnrollments(),
          ]);

          setProfile(profileRes.status === 'fulfilled' ? profileRes.value.data : null);

          if (enrRes.status === 'fulfilled') {
            const enrollments = enrRes.value.data || [];
            const rows = await Promise.all(
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
            setEnrolledRows(rows);
          }
        } else if (isTeacher) {
          const [profileRes, coursesRes, reviewsRes, enrRes] = await Promise.allSettled([
            getProfile(),
            getMyCourses(),
            getMyReviews(),
            getMyEnrollments(),
          ]);

          setProfile(profileRes.status === 'fulfilled' ? profileRes.value.data : null);
          setCourses(
            coursesRes.status === 'fulfilled' ? (coursesRes.value.data as unknown as Course[]) : []
          );
          setReviews(reviewsRes.status === 'fulfilled' ? reviewsRes.value.data : []);

          if (enrRes.status === 'fulfilled') {
            const enrollments = enrRes.value.data || [];
            const rows = await Promise.all(
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
            setEnrolledRows(rows);
          }
        } else {
          const [profileRes, coursesRes, reviewsRes] = await Promise.allSettled([
            getProfile(),
            getMyCourses(),
            getMyReviews(),
          ]);

          setProfile(profileRes.status === 'fulfilled' ? profileRes.value.data : null);
          setCourses(
            coursesRes.status === 'fulfilled' ? (coursesRes.value.data as unknown as Course[]) : []
          );
          setReviews(reviewsRes.status === 'fulfilled' ? reviewsRes.value.data : []);
        }
      } catch {
        setProfile(null);
        setCourses([]);
        setReviews([]);
        setEnrolledRows([]);
      } finally {
        setLoading(false);
      }
    };

    fetchAll();
  }, [isStudent, isTeacher, isViewMode, userId]);

  const handleSaveProfile = async (updated: Partial<UserProfile>) => {
    try {
      await updateProfile(updated);
      setProfile((prev) => (prev ? { ...prev, ...updated } : prev));
    } catch {
      setProfile((prev) => (prev ? { ...prev, ...updated } : prev));
    }
  };

  const filteredCourses = courses.filter((c) => {
    if (!c.title.toLowerCase().includes(search.toLowerCase())) return false;
    if (levelFilter !== 'All' && c.level?.toLowerCase() !== levelFilter.toLowerCase()) return false;
    if (categoryFilter !== 'All' && c.category?.toLowerCase() !== categoryFilter.toLowerCase()) return false;
    if (ratingFilter !== 'All') {
      const min = parseInt(ratingFilter, 10);
      if (!c.avgRating || c.avgRating < min) return false;
    }
    return true;
  });

  const filteredEnrolled = enrolledRows.filter((r) => {
    const title = r.course?.title ?? '';
    if (!title.toLowerCase().includes(enrollSearch.toLowerCase())) return false;
    if (enrollLevelFilter !== 'All' && r.course?.level?.toLowerCase() !== enrollLevelFilter.toLowerCase()) return false;
    if (enrollCategoryFilter !== 'All' && r.course?.category?.toLowerCase() !== enrollCategoryFilter.toLowerCase()) return false;
    return true;
  });

  const initials = profile?.name
    ? profile.name
        .split(' ')
        .map((w) => w[0])
        .join('')
        .toUpperCase()
        .slice(0, 2)
    : '?';

  const showTeacherInfo = isViewMode
    ? ['TEACHER', 'EDUCATOR', 'teacher', 'educator'].includes(profile?.role ?? '')
    : !isStudent;

  const socialEntries = Object.entries(profile?.socialLinks || {}).filter(([, url]) => !!url);

  const socialLabel: Record<string, string> = {
    twitter: '𝕏',
    linkedin: 'in',
    github: 'gh',
    website: 'web',
  };

  if (loading) {
    return (
      <div className="profile-page">
        <Navbar />
        <div style={{ textAlign: 'center', padding: '4rem', fontFamily: 'var(--font-mono)', fontStyle: 'italic', color: 'var(--text-muted)' }}>
          Loading profile...
        </div>
      </div>
    );
  }

  const EnrolledGrid: React.FC<{ rows: EnrollmentRow[] }> = ({ rows }) =>
    rows.length === 0 ? (
      <p className="profile-empty">No enrolled courses found.</p>
    ) : (
      <div className="profile-courses-grid">
        {rows.map((r) => {
          const c = r.course;
          const pct = r.progress?.progressPercent ?? 0;
          const done = r.progress?.completed;
          return (
            <Link
              to={`/courses/${r.enrollment.courseId}/learn`}
              key={r.enrollment.id}
              className="profile-course-card"
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <p className="pcc-topic" style={{ margin: 0 }}>{c?.title || 'Course'}</p>
                {done && (
                  <span style={{ fontSize: '0.7rem', background: '#22c55e', color: '#fff', borderRadius: '4px', padding: '1px 6px', marginLeft: '6px', whiteSpace: 'nowrap' }}>Completed</span>
                )}
              </div>
              <div className="pcc-thumbnail">
                {c?.thumbnail ? (
                  <img src={c.thumbnail} alt={c.title} />
                ) : (
                  <span className="pcc-thumb-placeholder">Introduction Picture or Video</span>
                )}
              </div>
              <div className="pcc-tags">
                {c?.level && <span className="pcc-tag">{c.level}</span>}
                {c?.category && <span className="pcc-tag">{c.category}</span>}
              </div>
              {c?.teacherName && (
                <span
                  className="pcc-author author-link"
                  onClick={(e) => { e.preventDefault(); e.stopPropagation(); navigate(`/profile/${c.teacherId}`); }}
                >
                  {c.teacherName}
                </span>
              )}
              <div className="pcc-footer">
                <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <div style={{ flex: 1, height: '4px', background: 'var(--border)', borderRadius: '2px', overflow: 'hidden' }}>
                    <div style={{ width: `${pct}%`, height: '100%', background: 'var(--accent)', borderRadius: '2px' }} />
                  </div>
                  <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>{pct}%</span>
                </div>
              </div>
            </Link>
          );
        })}
      </div>
    );

  return (
    <div className="profile-page">
      <Navbar />

      <div className="profile-body">
        <aside className="profile-sidebar">
          <div
            className="profile-avatar-wrap"
            title="Change photo"
            onClick={() => {/* future: open avatar upload */}}
          >
            {profile?.avatarUrl ? (
              <img className="profile-avatar-img" src={profile.avatarUrl} alt={profile.name} />
            ) : (
              <span className="profile-avatar-placeholder">{initials}</span>
            )}
            <div className="avatar-overlay">Change Photo</div>
          </div>

          <p className="profile-name">{profile?.name || 'Name'}</p>

          {showTeacherInfo && (
            <div className="teacher-badge">
              <span className="teacher-badge-check">✓</span>
              <span className="teacher-badge-label">Qualified Teacher</span>
            </div>
          )}

          {showTeacherInfo && (
            <>
              {socialEntries.length > 0 && (
                <div className="profile-social-links">
                  {socialEntries.map(([key, url]) => (
                    <a
                      key={key}
                      className="social-chip"
                      href={url as string}
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      {socialLabel[key] || key}
                    </a>
                  ))}
                </div>
              )}

              {!socialEntries.length && !isViewMode && (
                <p className="profile-bio" style={{ opacity: 0.5 }}>Social media links</p>
              )}

              <p className="profile-bio">{profile?.bio || (!isViewMode ? 'Short Introduction' : '')}</p>
            </>
          )}

          <div className="profile-stat-row">
            {showTeacherInfo && (
              <p className="profile-stat">
                <span>{profile?.courseCount ?? courses.length}</span> courses created
              </p>
            )}
            {!isViewMode && (
              <p className="profile-stat">
                <span>{(isStudent || isTeacher) ? enrolledRows.length : (profile?.enrolledCount ?? 0)}</span> enrolled courses
              </p>
            )}
          </div>

          {!isViewMode && (
            <button className="profile-edit-btn" onClick={() => setShowEditModal(true)}>
              Edit Profile
            </button>
          )}

          {!isViewMode && (['TEACHER', 'ADMIN'].includes(role)) && (
            <button className="profile-create-course-btn" onClick={() => navigate('/courses/create')}>
              + Create Course
            </button>
          )}
        </aside>

        <main className="profile-main">

          {(isViewMode || isTeacher || (!isStudent && !isTeacher)) && (
            <div className="profile-section">
              <div className="profile-section-header">
                <span className="profile-section-title">
                  {isTeacher ? 'My Created Courses' : 'Courses'}
                </span>
                <div className="courses-toolbar-row">
                  {showSearch && (
                    <input
                      className="courses-search-input"
                      type="text"
                      placeholder="Search courses..."
                      value={search}
                      onChange={(e) => setSearch(e.target.value)}
                      autoFocus
                    />
                  )}
                  <button
                    className="toolbar-filter-btn"
                    onClick={() => {
                      setShowSearch((s) => !s);
                      if (showSearch) setSearch('');
                    }}
                  >
                    Search
                  </button>

                  <div className="filter-btn-wrap">
                    <button
                      className={`toolbar-filter-btn${showFilters ? ' active' : ''}`}
                      onClick={() => setShowFilters((s) => !s)}
                    >
                      Filters
                    </button>
                    {showFilters && (
                      <div className="filter-panel filter-panel-3col">
                        <div className="filter-group">
                          <span className="filter-group-label">Level</span>
                          <div className="filter-chips">
                            {['All', 'Beginner', 'Intermediate', 'Advanced'].map((opt) => (
                              <button
                                key={opt}
                                className={`filter-chip ${levelFilter === opt ? 'active' : ''}`}
                                onClick={() => setLevelFilter(opt)}
                              >
                                {opt}
                              </button>
                            ))}
                          </div>
                        </div>
                        <div className="filter-group">
                          <span className="filter-group-label">Category</span>
                          <div className="filter-chips">
                            {['All', 'Programming', 'Mathematics', 'Physics', 'Sciences'].map((opt) => (
                              <button
                                key={opt}
                                className={`filter-chip ${categoryFilter === opt ? 'active' : ''}`}
                                onClick={() => setCategoryFilter(opt)}
                              >
                                {opt}
                              </button>
                            ))}
                          </div>
                        </div>
                        <div className="filter-group">
                          <span className="filter-group-label">Rating</span>
                          <div className="filter-chips">
                            {['All', '1', '2', '3', '4'].map((opt) => (
                              <button
                                key={opt}
                                className={`filter-chip ${ratingFilter === opt ? 'active' : ''}`}
                                onClick={() => setRatingFilter(opt)}
                              >
                                {opt === 'All' ? 'All' : `${opt}★+`}
                              </button>
                            ))}
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {filteredCourses.length === 0 ? (
                <p className="profile-empty">No courses found.</p>
              ) : (
                <div className="profile-courses-grid">
                  {filteredCourses.map((course) => (
                    <Link
                      to={isViewMode ? `/courses/${course.id}` : `/courses/${course.id}/edit`}
                      key={course.id}
                      className="profile-course-card"
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <p className="pcc-topic" style={{ margin: 0 }}>{course.title}</p>
                        {!course.published && (
                          <span style={{ fontSize: '0.7rem', background: '#f59e0b', color: '#fff', borderRadius: '4px', padding: '1px 6px', marginLeft: '6px', whiteSpace: 'nowrap' }}>Draft</span>
                        )}
                      </div>
                      <div className="pcc-thumbnail">
                        {course.thumbnail ? (
                          <img src={course.thumbnail} alt={course.title} />
                        ) : (
                          <span className="pcc-thumb-placeholder">Introduction Picture or Video</span>
                        )}
                      </div>
                      <div className="pcc-tags">
                        {course.level && <span className="pcc-tag">{course.level}</span>}
                        {course.category && <span className="pcc-tag">{course.category}</span>}
                      </div>
                      <div className="pcc-footer">
                        <span className="pcc-rate">
                          {course.avgRating ? (
                            <><StarRating rating={course.avgRating} /> {course.avgRating.toFixed(1)}</>
                          ) : (
                            'No rating'
                          )}
                        </span>
                        <span className="pcc-price">
                          {course.price ? `$${course.price}` : 'Free'}
                        </span>
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </div>
          )}

          {!isViewMode && isTeacher && (
            <div className="profile-section">
              <div className="profile-section-header">
                <span className="profile-section-title">My Enrolled Courses</span>
                <div className="courses-toolbar-row">
                  {showEnrollSearch && (
                    <input
                      className="courses-search-input"
                      type="text"
                      placeholder="Search enrolled courses..."
                      value={enrollSearch}
                      onChange={(e) => setEnrollSearch(e.target.value)}
                      autoFocus
                    />
                  )}
                  <button
                    className="toolbar-filter-btn"
                    onClick={() => {
                      setShowEnrollSearch((s) => !s);
                      if (showEnrollSearch) setEnrollSearch('');
                    }}
                  >
                    Search
                  </button>

                  <div className="filter-btn-wrap">
                    <button
                      className={`toolbar-filter-btn${showEnrollFilters ? ' active' : ''}`}
                      onClick={() => setShowEnrollFilters((s) => !s)}
                    >
                      Filters
                    </button>
                    {showEnrollFilters && (
                      <div className="filter-panel">
                        <div className="filter-group">
                          <span className="filter-group-label">Level</span>
                          <div className="filter-chips">
                            {['All', 'Beginner', 'Intermediate', 'Advanced'].map((opt) => (
                              <button
                                key={opt}
                                className={`filter-chip ${enrollLevelFilter === opt ? 'active' : ''}`}
                                onClick={() => setEnrollLevelFilter(opt)}
                              >
                                {opt}
                              </button>
                            ))}
                          </div>
                        </div>
                        <div className="filter-group">
                          <span className="filter-group-label">Category</span>
                          <div className="filter-chips">
                            {['All', 'Programming', 'Mathematics', 'Physics', 'Sciences'].map((opt) => (
                              <button
                                key={opt}
                                className={`filter-chip ${enrollCategoryFilter === opt ? 'active' : ''}`}
                                onClick={() => setEnrollCategoryFilter(opt)}
                              >
                                {opt}
                              </button>
                            ))}
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              <EnrolledGrid rows={filteredEnrolled} />
            </div>
          )}

          {!isViewMode && isStudent && (
            <div className="profile-section">
              <div className="profile-section-header">
                <span className="profile-section-title">My Enrolled Courses</span>
                <div className="courses-toolbar-row">
                  <div className="filter-btn-wrap">
                    <button
                      className={`toolbar-filter-btn${showEnrollFilters ? ' active' : ''}`}
                      onClick={() => setShowEnrollFilters((s) => !s)}
                    >
                      Filters
                    </button>
                    {showEnrollFilters && (
                      <div className="filter-panel">
                        <div className="filter-group">
                          <span className="filter-group-label">Level</span>
                          <div className="filter-chips">
                            {['All', 'Beginner', 'Intermediate', 'Advanced'].map((opt) => (
                              <button
                                key={opt}
                                className={`filter-chip ${enrollLevelFilter === opt ? 'active' : ''}`}
                                onClick={() => setEnrollLevelFilter(opt)}
                              >
                                {opt}
                              </button>
                            ))}
                          </div>
                        </div>
                        <div className="filter-group">
                          <span className="filter-group-label">Category</span>
                          <div className="filter-chips">
                            {['All', 'Programming', 'Mathematics', 'Physics', 'Sciences'].map((opt) => (
                              <button
                                key={opt}
                                className={`filter-chip ${enrollCategoryFilter === opt ? 'active' : ''}`}
                                onClick={() => setEnrollCategoryFilter(opt)}
                              >
                                {opt}
                              </button>
                            ))}
                          </div>
                        </div>
                      </div>
                    )}
                  </div>

                  {showEnrollSearch && (
                    <input
                      className="courses-search-input"
                      type="text"
                      placeholder="Search enrolled courses..."
                      value={enrollSearch}
                      onChange={(e) => setEnrollSearch(e.target.value)}
                      autoFocus
                    />
                  )}
                  <button
                    className="toolbar-filter-btn"
                    onClick={() => {
                      setShowEnrollSearch((s) => !s);
                      if (showEnrollSearch) setEnrollSearch('');
                    }}
                  >
                    Search
                  </button>
                </div>
              </div>

              <EnrolledGrid rows={filteredEnrolled} />
            </div>
          )}

          {(!isStudent || isViewMode) && (
            <div className="profile-section">
              <p className="reviews-title">Reviews from all courses</p>
              {reviews.length === 0 ? (
                <p className="profile-empty">No reviews yet.</p>
              ) : (
                <div className="profile-reviews-list">
                  {reviews.map((review) => (
                    <ReviewCard key={review.id} review={review} />
                  ))}
                </div>
              )}
            </div>
          )}
        </main>
      </div>

      {showEditModal && profile && (
        <EditProfileModal
          profile={profile}
          onClose={() => setShowEditModal(false)}
          onSave={handleSaveProfile}
        />
      )}
    </div>
  );
};

export default ProfilePage;
