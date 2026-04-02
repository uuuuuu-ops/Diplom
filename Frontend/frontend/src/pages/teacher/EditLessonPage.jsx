import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getLessonById, updateLesson } from "../../services/courseService";

export default function EditLessonPage() {
  const { lessonId, courseId } = useParams();
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [orderIndex, setOrderIndex] = useState("");
  const [duration, setDuration] = useState("");
  const [lectureText, setLectureText] = useState("");
  const [published, setPublished] = useState(false);

  const [videoFile, setVideoFile] = useState(null);
  const [lecturePdfFile, setLecturePdfFile] = useState(null);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadLesson() {
      try {
        setLoading(true);
        setError("");

        const lesson = await getLessonById(lessonId);

        setTitle(lesson.title || "");
        setDescription(lesson.description || "");
        setOrderIndex(lesson.orderIndex ?? "");
        setDuration(lesson.duration ?? "");
        setLectureText(lesson.lectureText || "");
        setPublished(lesson.published ?? false);
      } catch (err) {
        console.error("Failed to load lesson:", err);
        setError(err.response?.data?.message || "Failed to load lesson");
      } finally {
        setLoading(false);
      }
    }

    loadLesson();
  }, [lessonId]);

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      setSaving(true);
      setError("");

      const formData = new FormData();

      if (title !== "") formData.append("title", title);
      if (description !== "") formData.append("description", description);
      if (orderIndex !== "") formData.append("orderIndex", orderIndex);
      if (duration !== "") formData.append("duration", duration);
      if (lectureText !== "") formData.append("lectureText", lectureText);

      formData.append("published", String(published));

      if (videoFile) {
        formData.append("videoFile", videoFile);
      }

      if (lecturePdfFile) {
        formData.append("lecturePdfFile", lecturePdfFile);
      }

      await updateLesson(lessonId, formData);

      alert("Lesson updated successfully");
      navigate(`/teacher/courses/${courseId}/edit`);
    } catch (err) {
      console.error("Failed to update lesson:", err);
      console.error("STATUS:", err.response?.status);
      console.error("DATA:", err.response?.data);
      setError(err.response?.data?.message || "Failed to update lesson");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <div className="p-6 text-white">Loading lesson...</div>;
  }

  return (
    <div className="min-h-screen bg-neutral-950 px-6 py-10 text-white">
      <div className="mx-auto max-w-3xl">
        <h1 className="mb-6 text-3xl font-bold">Edit Lesson</h1>

        {error && (
          <div className="mb-4 rounded-xl border border-red-500/40 bg-red-500/10 p-4 text-red-300">
            {error}
          </div>
        )}

        <form
          onSubmit={handleSubmit}
          className="space-y-5 rounded-2xl border border-white/10 bg-white/5 p-6"
        >
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
              className="min-h-[100px] w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div className="grid gap-5 md:grid-cols-2">
            <div>
              <label className="mb-2 block text-sm font-medium">Order Index</label>
              <input
                type="number"
                className="w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
                value={orderIndex}
                onChange={(e) => setOrderIndex(e.target.value)}
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm font-medium">Duration (minutes)</label>
              <input
                type="number"
                className="w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
                value={duration}
                onChange={(e) => setDuration(e.target.value)}
                required
              />
            </div>
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium">Lecture Text</label>
            <textarea
              className="min-h-[180px] w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3 outline-none"
              value={lectureText}
              onChange={(e) => setLectureText(e.target.value)}
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium">Replace Video</label>
            <input
              type="file"
              accept="video/*"
              className="block w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3"
              onChange={(e) => setVideoFile(e.target.files[0] || null)}
            />
          </div>

          <div>
            <label className="mb-2 block text-sm font-medium">Replace Lecture PDF</label>
            <input
              type="file"
              accept="application/pdf"
              className="block w-full rounded-xl border border-white/10 bg-neutral-900 px-4 py-3"
              onChange={(e) => setLecturePdfFile(e.target.files[0] || null)}
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
            className="rounded-xl bg-blue-600 px-5 py-3 font-semibold transition hover:bg-blue-500 disabled:opacity-50"
          >
            {saving ? "Saving..." : "Update Lesson"}
          </button>
        </form>
      </div>
    </div>
  );
}