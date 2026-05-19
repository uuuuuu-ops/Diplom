import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import {
  getLessonById,
  getLessonsByCourse,
  getLessonComments,
  getLessonCommentReplies,
  addLessonComment,
  markCommentAsAnswer,
  deleteLessonComment,
  completeLesson,
  getCourseProgress,
  isLessonUnlocked,
  getQuizByLessonId,
  Lesson,
  LessonComment,
  CourseProgress,
  Quiz,
  API_BASE,
} from '../api';
import { isAuthenticated, getUserId } from '../api/auth';
import './css/LessonPage.css';

const formatTimeAgo = (iso: string): string => {
  const diff = Date.now() - new Date(iso).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
};

interface CommentItemProps {
  comment: LessonComment;
  lessonId: string;
  isTeacher: boolean;
  currentUserId: string | null;
  onReply: (parentId: string) => void;
  onMark: (commentId: string) => void;
  onDelete: (commentId: string) => void;
}

const CommentItem: React.FC<CommentItemProps> = ({
  comment,
  lessonId,
  isTeacher,
  currentUserId,
  onReply,
  onMark,
  onDelete,
}) => {
  const [replies, setReplies] = useState<LessonComment[]>([]);
  const [showReplies, setShowReplies] = useState(false);
  const [loadingReplies, setLoadingReplies] = useState(false);

  const handleToggleReplies = async () => {
    if (!showReplies && replies.length === 0) {
      setLoadingReplies(true);
      try {
        const res = await getLessonCommentReplies(lessonId, comment.id);
        setReplies(res.data || []);
      } catch {
        setReplies([]);
      } finally {
        setLoadingReplies(false);
      }
    }
    setShowReplies((s) => !s);
  };

  const initials = (comment.authorName || '?')
    .split(' ')
    .map((p: string) => p[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  return (
    <div className={`lesson-comment ${comment.markedAsAnswer ? 'is-answer' : ''}`}>
      <div className="lc-avatar">
        {comment.authorAvatarUrl
          ? <img src={comment.authorAvatarUrl} alt="" />
          : initials}
      </div>
      <div className="lc-body">
        <div className="lc-header">
          <span className="lc-name">{comment.authorName || 'User'}</span>
          {comment.markedAsAnswer && <span className="lc-answer-badge">Teacher answer</span>}
          <span className="lc-time">{formatTimeAgo(comment.createdAt)}</span>
        </div>
        <p className="lc-content">{comment.content}</p>
        <div className="lc-actions">
          <button className="lc-action-btn" onClick={() => onReply(comment.id)}>
            Reply
          </button>
          <button className="lc-action-btn" onClick={handleToggleReplies}>
            {showReplies ? 'Hide replies' : 'View replies'}
          </button>
          {isTeacher && !comment.markedAsAnswer && (
            <button className="lc-action-btn" onClick={() => onMark(comment.id)}>
              Mark as answer
            </button>
          )}
          {currentUserId && comment.authorId === currentUserId && (
            <button
              className="lc-action-btn lc-danger"
              onClick={() => onDelete(comment.id)}
            >
              Delete
            </button>
          )}
        </div>

        {showReplies && (
          <div className="lc-replies">
            {loadingReplies ? (
              <p className="lc-empty">Loading...</p>
            ) : replies.length === 0 ? (
              <p className="lc-empty">No replies yet.</p>
            ) : (
              replies.map((r) => (
                <div key={r.id} className="lc-reply">
                  <span className="lc-reply-name">{r.authorName || 'User'}</span>
                  <span className="lc-reply-time">{formatTimeAgo(r.createdAt)}</span>
                  <p className="lc-content">{r.content}</p>
                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
};

const LessonPage: React.FC = () => {
  const { courseId, lessonId } = useParams<{ courseId: string; lessonId: string }>();
  const navigate = useNavigate();

  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [siblings, setSiblings] = useState<Lesson[]>([]);
  const [progress, setProgress] = useState<CourseProgress | null>(null);
  const [comments, setComments] = useState<LessonComment[]>([]);
  const [quiz, setQuiz] = useState<Quiz | null>(null);
  const [unlocked, setUnlocked] = useState(true);
  const [loading, setLoading] = useState(true);
  const [completing, setCompleting] = useState(false);

  const [newComment, setNewComment] = useState('');
  const [replyTo, setReplyTo] = useState<string | null>(null);

  const isTeacher = (() => {
    try {
      const raw = localStorage.getItem('role');
      return raw?.toUpperCase() === 'TEACHER';
    } catch {
      return false;
    }
  })();

  const currentUserId = getUserId();

  useEffect(() => {
    if (!isAuthenticated()) {
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  useEffect(() => {
    if (!courseId || !lessonId) return;
    setLoading(true);
    Promise.allSettled([
      getLessonById(lessonId),
      getLessonsByCourse(courseId),
      getCourseProgress(courseId),
      getLessonComments(lessonId),
      getQuizByLessonId(lessonId),
      isLessonUnlocked(courseId, lessonId),
    ]).then(([lRes, sRes, pRes, cRes, qRes, uRes]) => {
      setLesson(lRes.status === 'fulfilled' ? lRes.value.data : null);
      setSiblings(sRes.status === 'fulfilled' ? sRes.value.data : []);
      setProgress(pRes.status === 'fulfilled' ? pRes.value.data : null);
      setComments(cRes.status === 'fulfilled' ? cRes.value.data : []);
      setQuiz(qRes.status === 'fulfilled' ? qRes.value.data : null);
      setUnlocked(uRes.status === 'fulfilled' ? uRes.value.data.unlocked : true);
      setLoading(false);
    });
  }, [courseId, lessonId]);

  const handleSubmitComment = async () => {
    if (!lessonId || !newComment.trim()) return;
    try {
      const res = await addLessonComment(lessonId, newComment.trim(), replyTo);
      if (replyTo) {
      } else {
        setComments((prev) => [res.data, ...prev]);
      }
      setNewComment('');
      setReplyTo(null);
    } catch {
    }
  };

  const handleMark = async (commentId: string) => {
    if (!lessonId) return;
    try {
      const res = await markCommentAsAnswer(lessonId, commentId);
      setComments((prev) =>
        prev.map((c) => (c.id === commentId ? res.data : { ...c, markedAsAnswer: false }))
      );
    } catch {
      setComments((prev) =>
        prev.map((c) => ({ ...c, markedAsAnswer: c.id === commentId }))
      );
    }
  };

  const handleDeleteComment = async (commentId: string) => {
    if (!lessonId) return;
    if (!window.confirm('Delete this comment?')) return;
    try {
      await deleteLessonComment(lessonId, commentId);
    } catch {
    }
    setComments((prev) => prev.filter((c) => c.id !== commentId));
  };

  const handleMarkComplete = async () => {
    if (!courseId || !lessonId) return;
    if (lesson?.quizRequired && !hasPassedQuiz()) {
      alert('You must pass the lesson quiz before completing this lesson.');
      return;
    }
    setCompleting(true);
    try {
      const res = await completeLesson(courseId, lessonId);
      setProgress(res.data);
      const idx = siblings.findIndex((l) => l.id === lessonId);
      if (idx >= 0 && idx < siblings.length - 1) {
        const next = siblings[idx + 1];
        navigate(`/courses/${courseId}/lessons/${next.id}`);
      }
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Could not mark lesson complete.');
    } finally {
      setCompleting(false);
    }
  };

  const hasPassedQuiz = (): boolean => {
    if (!quiz) return true;
    return progress?.passedQuizIds?.includes(quiz.id) ?? false;
  };

  const isCompleted = !!progress?.completedLessonIds?.includes(lessonId || '');

  if (loading) {
    return (
      <div className="lesson-page">
        <Navbar />
        <p className="lesson-loading">Loading lesson...</p>
      </div>
    );
  }

  if (!lesson) {
    return (
      <div className="lesson-page">
        <Navbar />
        <p className="lesson-loading">Lesson not found.</p>
      </div>
    );
  }

  if (!unlocked) {
    return (
      <div className="lesson-page">
        <Navbar />
        <div className="lesson-locked">
          <h2>This lesson is locked</h2>
          <p>Complete previous lessons (and any required quizzes) to unlock this one.</p>
          <button
            className="back-btn"
            onClick={() => navigate(`/courses/${courseId}/learn`)}
          >
            ← Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="lesson-page">
      <Navbar />

      <div className="lesson-topbar">
        <button className="back-btn" onClick={() => navigate(`/courses/${courseId}/learn`)}>
          ← Back
        </button>
        {progress && (
          <div className="lesson-progress-pill">
            <span>{progress.progressPercent}% complete</span>
            <div className="lesson-progress-bar">
              <div style={{ width: `${progress.progressPercent}%` }} />
            </div>
          </div>
        )}
      </div>

      <div className="lesson-layout">
        <aside className="lesson-sidebar">
          <h3 className="lesson-sidebar-title">Lessons</h3>
          <ul className="lesson-sidebar-list">
            {siblings.map((l, idx) => {
              const done = progress?.completedLessonIds?.includes(l.id);
              const active = l.id === lessonId;
              return (
                <li key={l.id}>
                  <Link
                    to={`/courses/${courseId}/lessons/${l.id}`}
                    className={`lesson-sidebar-item ${active ? 'active' : ''} ${done ? 'done' : ''}`}
                  >
                    <span className="ls-index">{idx + 1}.</span>
                    <span className="ls-title">{l.title}</span>
                    {done && <span className="ls-check">Done</span>}
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

        {/* Main */}
        <main className="lesson-main">
          <h1 className="lesson-title">{lesson.title}</h1>
          {lesson.description && <p className="lesson-desc">{lesson.description}</p>}

          {/* Video */}
          {(lesson.videoUrl || lesson.videoFileName) && (
          <div className="lesson-video-wrap">
            {lesson.videoUrl ? (
              <video controls className="lesson-video" src={lesson.videoUrl} />
            ) : (
              <video
                controls
                className="lesson-video"
                src={`${API_BASE}/files?path=${lesson.videoFileName}`}
              />
            )}
          </div>
          )}

          {/* Lecture text */}
          {lesson.lectureText && (
            <section className="lesson-text-section">
              <h3>Lecture notes</h3>
              <p className="lesson-text-body">{lesson.lectureText}</p>
            </section>
          )}

          {/* PDF */}
          {lesson.lecturePdfUrl && (
            <a
              className="lesson-pdf-btn"
              href={lesson.lecturePdfUrl}
              target="_blank"
              rel="noopener noreferrer"
            >
              Download lecture PDF
            </a>
          )}

          {/* Quiz */}
          {quiz && (isTeacher || quiz.published) && (
            <section className="lesson-quiz-card">
              <div>
                <h3>{quiz.title}</h3>
                <p className="lesson-quiz-meta">
                  {quiz.questions.length} questions · pass at {quiz.passingScore}%
                  {quiz.timeLimitSeconds
                    ? ` · ${Math.round(quiz.timeLimitSeconds / 60)} min limit`
                    : ''}
                </p>
                {isTeacher && !quiz.published && (
                  <p className="lesson-quiz-unpublished">Not published — students cannot see this quiz</p>
                )}
              </div>
              {!isTeacher && quiz.published && (
                <button
                  className="lesson-quiz-btn"
                  onClick={() => navigate(`/quizzes/${quiz.id}`)}
                >
                  {hasPassedQuiz() ? 'Review quiz' : 'Take quiz'}
                </button>
              )}
            </section>
          )}

          {/* Mark complete */}
          {!isTeacher && (
            <div className="lesson-complete-row">
              <button
                className="lesson-complete-btn"
                disabled={isCompleted || completing || (!!lesson.quizRequired && !hasPassedQuiz())}
                onClick={handleMarkComplete}
              >
                {isCompleted ? 'Completed' : completing ? 'Saving...' : 'Mark as complete'}
              </button>
              {lesson.quizRequired && !hasPassedQuiz() && (
                <span className="lesson-gate-note">Pass the quiz to unlock completion</span>
              )}
            </div>
          )}

          {/* Comments */}
          <section className="lesson-comments-section">
            <h3>Discussion</h3>

            <div className="lesson-comment-form">
              {replyTo && (
                <div className="reply-banner">
                  Replying to comment
                  <button onClick={() => setReplyTo(null)}>cancel</button>
                </div>
              )}
              <textarea
                className="lesson-comment-input"
                placeholder="Ask a question or share your thoughts..."
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                rows={3}
              />
              <button
                className="lesson-comment-submit"
                disabled={!newComment.trim()}
                onClick={handleSubmitComment}
              >
                Post
              </button>
            </div>

            {comments.length === 0 ? (
              <p className="lesson-empty">No comments yet. Start the discussion.</p>
            ) : (
              <div className="lesson-comments-list">
                {comments.map((c) => (
                  <CommentItem
                    key={c.id}
                    comment={c}
                    lessonId={lessonId!}
                    isTeacher={isTeacher}
                    currentUserId={currentUserId}
                    onReply={(id) => setReplyTo(id)}
                    onMark={handleMark}
                    onDelete={handleDeleteComment}
                  />
                ))}
              </div>
            )}
          </section>
        </main>
      </div>
    </div>
  );
};

export default LessonPage;
