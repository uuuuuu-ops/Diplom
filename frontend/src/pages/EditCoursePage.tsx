import React, { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import {
  getLessonsByCourse,
  deleteLesson,
  deleteCourse,
  Lesson,
  API_BASE,
} from '../api';
import { getCourseById, Course } from '../api/courses';
import { isAuthenticated } from '../api/auth';
import './css/EditCoursePage.css';
const CATEGORIES = ['Programming', 'Mathematics', 'Physics', 'Sciences'];

const EditCoursePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [course, setCourse] = useState<Course | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('');
  const [level, setLevel] = useState('Beginner');
  const [free, setFree] = useState(true);
  const [published, setPublished] = useState(false);
  const [thumbnail, setThumbnail] = useState<File | null>(null);
  const [thumbnailPreview, setThumbnailPreview] = useState<string | null>(null);
  const thumbnailInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!isAuthenticated()) navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    Promise.allSettled([getCourseById(id), getLessonsByCourse(id)]).then(([cRes, lRes]) => {
      const c = cRes.status === 'fulfilled' ? cRes.value.data : null;
      if (c) {
        setCourse(c);
        setTitle(c.title);
        setDescription(c.description || '');
        setCategory(c.category || '');
        setLevel(c.level || 'Beginner');
        setFree(c.free !== false);
        setPublished(c.published);
      }
      setLessons(lRes.status === 'fulfilled' ? lRes.value.data : []);
      setLoading(false);
    });
  }, [id]);

  const handleSave = async () => {
    if (!id) return;
    setSaving(true);
    setError('');
    try {
      const fd = new FormData();
      fd.append('title', title);
      fd.append('description', description);
      fd.append('category', category);
      fd.append('level', level);
      fd.append('free', String(free));
      fd.append('published', String(published));
      if (thumbnail) fd.append('thumbnail', thumbnail);

      const token = localStorage.getItem('token');
      await axios.put(`${API_BASE}/courses/${id}`, fd, {
        headers: {
          Authorization: token ? `Bearer ${token}` : '',
          'Content-Type': 'multipart/form-data',
        },
      });
      navigate(`/courses/${id}`);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Could not save course.');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteLesson = async (lessonId: string, title: string) => {
    if (!window.confirm(`Delete lesson "${title}"?`)) return;
    try {
      await deleteLesson(lessonId);
    } catch {
    }
    setLessons((prev) => prev.filter((l) => l.id !== lessonId));
  };

  const handleDeleteCourse = async () => {
    if (!id) return;
    if (!window.confirm('Permanently delete this course and all its lessons?')) return;
    try {
      await deleteCourse(id);
      navigate('/profile');
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not delete course.');
    }
  };

  if (loading) {
    return (
      <div className="edit-course-page">
        <Navbar />
        <p className="ec-loading">Loading course...</p>
      </div>
    );
  }

  if (!course) {
    return (
      <div className="edit-course-page">
        <Navbar />
        <p className="ec-loading">Course not found.</p>
      </div>
    );
  }

  return (
    <div className="edit-course-page">
      <Navbar />
      <div className="ec-container">
        <button className="back-btn" onClick={() => navigate(`/profile`)}>
          ← Back
        </button>

        <header className="ec-header">
          <h1>Edit course</h1>
          <p className="ec-sub">Update details, manage lessons, or unpublish this course.</p>
        </header>

        {error && <p className="ec-error">{error}</p>}

        <section className="ec-card">
          <h2>Course details</h2>

          <label className="ec-field">
            <span>Title</span>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="ec-input"
            />
          </label>

          <label className="ec-field">
            <span>Description</span>
            <textarea
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="ec-input"
            />
          </label>

          <div className="ec-field-row">
            <label className="ec-field">
              <span>Category</span>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="ec-input"
              >
                <option value="">Choose…</option>
                {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </label>
            <label className="ec-field">
              <span>Level</span>
              <select
                value={level}
                onChange={(e) => setLevel(e.target.value)}
                className="ec-input"
              >
                <option>Beginner</option>
                <option>Intermediate</option>
                <option>Advanced</option>
              </select>
            </label>
            <label className="ec-field">
              <span>Access</span>
              <select
                value={free ? 'free' : 'subscription'}
                onChange={(e) => setFree(e.target.value === 'free')}
                className="ec-input"
              >
                <option value="free">Free</option>
                <option value="subscription">Subscription only</option>
              </select>
            </label>
          </div>

          <div className="ec-field">
            <span>Thumbnail</span>
            <div className="ec-thumbnail-wrap">
              {(thumbnailPreview || course.thumbnail) && (
                <img
                  src={thumbnailPreview ?? course.thumbnail}
                  alt="Course thumbnail"
                  className="ec-thumbnail-preview"
                />
              )}
              <button
                type="button"
                className="ec-secondary-btn"
                onClick={() => thumbnailInputRef.current?.click()}
              >
                Replace thumbnail
              </button>
              <input
                ref={thumbnailInputRef}
                type="file"
                accept="image/*"
                style={{ display: 'none' }}
                onChange={(e) => {
                  const file = e.target.files?.[0] || null;
                  setThumbnail(file);
                  setThumbnailPreview(file ? URL.createObjectURL(file) : null);
                }}
              />
            </div>
          </div>

          <label className="ec-toggle">
            <input
              type="checkbox"
              checked={published}
              onChange={(e) => setPublished(e.target.checked)}
            />
            <span>Published</span>
          </label>

          <div className="ec-actions">
            <button className="ec-save-btn" disabled={saving} onClick={handleSave}>
              {saving ? 'Saving...' : 'Save changes'}
            </button>
            <button className="ec-secondary-btn" onClick={() => navigate(`/courses/${course.id}`)}>
              Cancel
            </button>
          </div>
        </section>

        <section className="ec-card">
          <div className="ec-section-head">
            <h2>Lessons ({lessons.length})</h2>
            <Link to={`/courses/${course.id}/lessons/new`} className="ec-add-btn">
              + Add lesson
            </Link>
          </div>

          {lessons.length === 0 ? (
            <p className="ec-empty">No lessons yet.</p>
          ) : (
            <ul className="ec-lesson-list">
              {lessons
                .slice()
                .sort((a, b) => a.orderIndex - b.orderIndex)
                .map((l) => (
                  <li key={l.id} className="ec-lesson-row">
                    <div>
                      <p className="ec-lesson-title">
                        <span className="ec-lesson-num">{l.orderIndex}.</span>
                        {l.title}
                      </p>
                      <p className="ec-lesson-meta">
                        {l.duration ? `${l.duration} min` : 'No duration set'}
                        {" · "}
                        {l.published ? 'Published' : 'Draft'}
                      </p>
                    </div>
                    <div className="ec-lesson-actions">
                      <Link
                        to={`/courses/${course.id}/lessons/${l.id}/edit`}
                        className="ec-secondary-btn"
                      >
                        Edit
                      </Link>
                      <button
                        className="ec-danger-btn"
                        onClick={() => handleDeleteLesson(l.id, l.title)}
                      >
                        Delete
                      </button>
                    </div>
                  </li>
                ))}
            </ul>
          )}
        </section>

        <section className="ec-card ec-danger-zone">
          <h2>Danger zone</h2>
          <p className="ec-warning">
            Deleting a course is permanent. Students lose access and you cannot recover lessons or progress.
          </p>
          <button className="ec-danger-btn" onClick={handleDeleteCourse}>
            Delete course
          </button>
        </section>
      </div>
    </div>
  );
};

export default EditCoursePage;

