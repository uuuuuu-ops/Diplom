import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  getCourseById,
  updateCourse,
  getLessonsByCourseId,
  deleteLesson,
} from "../../services/courseService";

export default function EditCoursePage() {
  const { courseId } = useParams();
  const navigate = useNavigate();

  const [course, setCourse] = useState(null);
  const [lessons, setLessons] = useState([]);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("");
  const [level, setLevel] = useState("BEGINNER");
  const [free, setFree] = useState(true);
  const [price, setPrice] = useState("");
  const [published, setPublished] = useState(false);
  const [thumbnailFile, setThumbnailFile] = useState(null);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
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
      setLessons(lessonsData || []);

      setTitle(courseData.title || "");
      setDescription(courseData.description || "");
      setCategory(courseData.category || "");
      setLevel(courseData.level || "BEGINNER");
      setFree(courseData.free ?? true);
      setPrice(courseData.price ? String(courseData.price) : "");
      setPublished(courseData.published ?? false);
    } catch (err) {
      console.error("Failed to load course:", err);
      console.error("DATA:", err.response?.data);
      setError(err.response?.data?.message || "Failed to load course");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [courseId]);

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      setSaving(true);
      setError("");

      const formData = new FormData();

      formData.append("title", title);
      formData.append("description", description);
      formData.append("category", category);
      formData.append("level", level);
      formData.append("free", String(free));
      formData.append("published", String(published));

      if (!free && price !== "") {
        formData.append("price", price);
      }

      if (thumbnailFile) {
        formData.append("thumbnail", thumbnailFile);
      }

      await updateCourse(courseId, formData);
      alert("Course updated successfully");
      navigate("/teacher/courses");
    } catch (err) {
      console.error("Failed to update course:", err);
      console.error("STATUS:", err.response?.status);
      console.error("DATA:", err.response?.data);
      setError(err.response?.data?.message || "Failed to update course");
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteLesson(lessonId) {
    const confirmed = window.confirm("Are you sure you want to delete this lesson?");
    if (!confirmed) return;

    try {
      await deleteLesson(lessonId);
      setLessons((prev) => prev.filter((lesson) => lesson.id !== lessonId));
    } catch (err) {
      console.error("Failed to delete lesson:", err);
      alert(err.response?.data?.message || "Failed to delete lesson");
    }
  }

  if (loading) {
    return <div className="p-6 text-white">Loading course...</div>;
  }

  return (
    <div className="min-h-screen bg-neutral-950 px-6 py-10 text-white">
      <div className="mx-auto max-w-6xl">
        <div className="mb-8 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-3xl font-bold">Edit Course</h1>
            <p className="mt-2 text-sm text-white/60">
              Update course information and manage course lessons
            </p>
          </div>

          <Link
            to={`/teacher/courses/${courseId}/lessons/new`}
            className="rounded-xl bg-blue-600 px-4 py-3 text-sm font-semibold hover:bg-blue-500"
          >
            + Add Lesson
          </Link>
        </div>

        {error && (
          <div className="mb-6 rounded-xl border border-red-500/40 bg-red-500/10 p-4 text-red-300">
            {error}
          </div>
        )}

        <div className="grid gap-8 lg:grid-cols-[1.2fr_0.8fr]">
          <div className="rounded-2xl border border-white/10 bg-white/5 p-6">
            <h2 className="mb-5 text-xl font-semibold">Course Details</h2>

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="mb-2 block text-sm font-medium">Title</label>
                <input
                  type="text"
                  className="w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">Description</label>
                <textarea
                  className="min-h-[120px] w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">Category</label>
                <input
                  type="text"
                  className="w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
                  value={category}
                  onChange={(e) => setCategory(e.target.value)}
                  required
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">Level</label>
                <select
                  className="w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
                  value={level}
                  onChange={(e) => setLevel(e.target.value)}
                >
                  <option value="BEGINNER">BEGINNER</option>
                  <option value="INTERMEDIATE">INTERMEDIATE</option>
                  <option value="ADVANCED">ADVANCED</option>
                </select>
              </div>

              <div>
                <label className="mb-2 block text-sm font-medium">Course Type</label>
                <select
                  className="w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
                  value={String(free)}
                  onChange={(e) => setFree(e.target.value === "true")}
                >
                  <option value="true">Free</option>
                  <option value="false">Paid</option>
                </select>
              </div>

              {!free && (
                <div>
                  <label className="mb-2 block text-sm font-medium">Price</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    className="w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    required={!free}
                  />
                </div>
              )}

              <div>
                <label className="mb-2 block text-sm font-medium">Replace Thumbnail</label>
                <input
                  type="file"
                  accept="image/*"
                  className="block w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3"
                  onChange={(e) => setThumbnailFile(e.target.files[0] || null)}
                />
              </div>

              <div className="flex items-center gap-3">
                <input
                  id="published"
                  type="checkbox"
                  checked={published}
                  onChange={(e) => setPublished(e.target.checked)}
                />
                <label htmlFor="published" className="text-sm font-medium">
                  Published
                </label>
              </div>

              <button
                type="submit"
                disabled={saving}
                className="rounded-xl bg-green-600 px-5 py-3 font-semibold transition hover:bg-green-500 disabled:opacity-50"
              >
                {saving ? "Saving..." : "Update Course"}
              </button>
            </form>
          </div>

          <div className="rounded-2xl border border-white/10 bg-white/5 p-6">
            <div className="mb-5 flex items-center justify-between">
              <h2 className="text-xl font-semibold">Lessons</h2>
              <span className="rounded-full bg-white/10 px-3 py-1 text-xs text-white/70">
                {lessons.length} total
              </span>
            </div>

            {lessons.length === 0 ? (
              <div className="rounded-xl border border-dashed border-white/10 p-5 text-sm text-white/60">
                No lessons yet
              </div>
            ) : (
              <div className="space-y-4">
                {lessons
                  .slice()
                  .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0))
                  .map((lesson) => (
                    <div
                      key={lesson.id}
                      className="rounded-xl border border-white/10 bg-neutral-900 p-4"
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <h3 className="font-semibold">{lesson.title}</h3>
                          <p className="mt-1 text-sm text-white/60">
                            Order: {lesson.orderIndex ?? "-"} • Duration:{" "}
                            {lesson.duration ?? 0} min
                          </p>
                          <p className="mt-1 text-xs text-white/50">
                            {lesson.published ? "Published" : "Draft"}
                          </p>
                        </div>

                        <div className="flex gap-2">
                          <Link
                            to={`/teacher/courses/${courseId}/lessons/${lesson.id}/edit`}
                            className="rounded-lg bg-yellow-600 px-3 py-2 text-sm font-medium hover:bg-yellow-500"
                          >
                            Edit
                          </Link>

                          <button
                            type="button"
                            onClick={() => handleDeleteLesson(lesson.id)}
                            className="rounded-lg bg-red-600 px-3 py-2 text-sm font-medium hover:bg-red-500"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}