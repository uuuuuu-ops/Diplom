import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import {
  getCourseById,
  getLessonsByCourseId,
} from "../../services/courseService";

export default function StudentCourseDetailsPage() {
  const { courseId } = useParams();

  const [course, setCourse] = useState(null);
  const [lessons, setLessons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadData() {
    try {
      setLoading(true);
      setError("");

      const [courseData, lessonsData] = await Promise.all([
        getCourseById(courseId),
        getLessonsByCourseId(courseId),
      ]);

      setCourse(courseData);
      setLessons(
        (lessonsData || []).slice().sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
      );
    } catch (err) {
      console.error("Failed to load course page:", err);
      console.error("STATUS:", err.response?.status);
      console.error("DATA:", err.response?.data);
      setError(err.response?.data?.message || "Failed to load course");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [courseId]);

  if (loading) {
    return <div className="p-6 text-white">Loading course...</div>;
  }

  if (error) {
    return <div className="p-6 text-red-400">{error}</div>;
  }

  if (!course) {
    return <div className="p-6 text-white">Course not found</div>;
  }

  return (
    <div className="min-h-screen bg-[#071a2f] px-6 py-10 text-white">
      <div className="mx-auto max-w-7xl">
        <div className="grid gap-8 lg:grid-cols-[1.4fr_0.8fr]">
          <div>
            <div className="mb-8 rounded-3xl border border-white/10 bg-white/5 p-6">
              <h1 className="text-4xl font-bold">{course.title}</h1>
              <p className="mt-4 max-w-3xl text-white/70">{course.description}</p>

              <div className="mt-6 flex flex-wrap gap-3">
                <span className="rounded-full bg-white/10 px-4 py-2 text-sm">
                  {course.level || "BEGINNER"}
                </span>
                <span className="rounded-full bg-white/10 px-4 py-2 text-sm">
                  {course.free ? "Free" : `${course.price ?? 0} USD`}
                </span>
                <span className="rounded-full bg-white/10 px-4 py-2 text-sm">
                  {lessons.length} lessons
                </span>
              </div>
            </div>

            <div className="rounded-3xl border border-white/10 bg-white/5 p-6">
              <h2 className="text-3xl font-bold">Course content</h2>
              <p className="mt-2 text-white/60">
                Structured lessons and course resources.
              </p>

              <div className="mt-6 space-y-4">
                {lessons.length === 0 ? (
                  <div className="rounded-2xl border border-dashed border-white/10 p-6 text-white/60">
                    No lessons yet
                  </div>
                ) : (
                  lessons.map((lesson, index) => {
                    const isPreview = index < 2 || lesson.preview === true;
                    const isLocked = !isPreview && !course.free;

                    return (
                      <div
                        key={lesson.id}
                        className="flex items-center justify-between gap-4 rounded-2xl border border-white/10 bg-white/5 p-4"
                      >
                        <div className="flex min-w-0 items-center gap-4">
                          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-blue-500/20 text-lg">
                            {isLocked ? "🔒" : "▶"}
                          </div>

                          <div className="min-w-0">
                            <h3 className="truncate text-lg font-semibold">
                              {lesson.title}
                            </h3>
                            <div className="mt-1 flex flex-wrap items-center gap-3 text-sm text-white/60">
                              <span>{lesson.duration ?? 0} min</span>
                              {isPreview && (
                                <span className="rounded-full bg-emerald-500/15 px-2 py-1 text-xs text-emerald-300">
                                  Preview
                                </span>
                              )}
                              {lesson.published === false && (
                                <span className="rounded-full bg-yellow-500/15 px-2 py-1 text-xs text-yellow-300">
                                  Draft
                                </span>
                              )}
                            </div>
                          </div>
                        </div>

                        {isLocked ? (
                          <button
                            type="button"
                            className="rounded-xl border border-white/10 px-4 py-2 text-sm text-white/80"
                          >
                            Locked
                          </button>
                        ) : (
                          <Link
                            to={`/student/courses/${courseId}/lessons/${lesson.id}`}
                            className="rounded-xl bg-blue-600 px-4 py-2 text-sm font-medium hover:bg-blue-500"
                          >
                            Open
                          </Link>
                        )}
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>

          <div>
            <div className="sticky top-6 rounded-3xl border border-white/10 bg-white/5 p-6">
              <div className="mb-6 text-4xl font-bold">
                {course.free ? "Free" : `${course.price ?? 0} USD`}
              </div>

              <button className="mb-3 w-full rounded-2xl bg-blue-600 px-4 py-3 font-semibold hover:bg-blue-500">
                {course.free ? "Start Course" : "Buy Course"}
              </button>

              {!course.free && (
                <button className="mb-6 w-full rounded-2xl border border-white/10 bg-white/5 px-4 py-3 font-semibold hover:bg-white/10">
                  Subscribe for Access
                </button>
              )}

              <div className="space-y-4 text-sm">
                <div className="flex items-center justify-between">
                  <span className="text-white/60">Access type</span>
                  <span>{course.free ? "Free" : "Paid / Subscription"}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-white/60">Lessons</span>
                  <span>{lessons.length}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-white/60">Level</span>
                  <span>{course.level || "BEGINNER"}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-white/60">Certificate</span>
                  <span>Available</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}