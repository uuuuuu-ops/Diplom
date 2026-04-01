import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { createLesson } from "../../services/courseService";

export default function CreateLessonPage() {
  const { courseId } = useParams();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    title: "",
    description: "",
    orderIndex: "",
    duration: "",
    lectureText: "",
  });

  const [videoFile, setVideoFile] = useState(null);
  const [lecturePdfFile, setLecturePdfFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function handleVideoChange(e) {
    setVideoFile(e.target.files[0] || null);
  }

  function handlePdfChange(e) {
    setLecturePdfFile(e.target.files[0] || null);
  }

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      setLoading(true);
      setError("");
      setSuccess("");

      const formData = new FormData();
      formData.append("title", form.title.trim());
      formData.append("description", form.description.trim());
      formData.append("orderIndex", form.orderIndex);
      formData.append("duration", form.duration);
      formData.append("lectureText", form.lectureText.trim());

      if (videoFile) {
        formData.append("video", videoFile);
      }

      if (lecturePdfFile) {
        formData.append("lecturePdf", lecturePdfFile);
      }

      await createLesson(courseId, formData);

      setSuccess("Lesson created successfully.");

      setTimeout(() => {
        navigate("/teacher/courses");
      }, 800);
    } catch (err) {
      console.error("Create lesson failed:", err);

      const message =
        err?.response?.data?.message ||
        err?.response?.data ||
        err?.message ||
        "Failed to create lesson.";

      setError(typeof message === "string" ? message : "Failed to create lesson.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="px-6 py-10">
      <div className="mx-auto max-w-4xl">
        <div className="mb-8">
          <p className="mb-3 text-sm uppercase tracking-[0.24em] text-cyan-300">
            Teacher Dashboard
          </p>
          <h1 className="text-4xl font-bold text-white">Create Lesson</h1>
          <p className="mt-3 max-w-2xl text-lg leading-8 text-slate-300">
            Add a new lesson to your draft course with video, PDF, and lecture text.
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="rounded-[32px] border border-white/10 bg-[#0d1b2a]/80 p-8 shadow-[0_20px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl"
        >
          <div className="grid gap-5 md:grid-cols-2">
            <div className="md:col-span-2">
              <label className="mb-2 block text-sm text-slate-300">Lesson title</label>
              <input
                name="title"
                value={form.title}
                onChange={handleChange}
                placeholder="Introduction to Controllers"
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-cyan-400"
                required
              />
            </div>

            <div className="md:col-span-2">
              <label className="mb-2 block text-sm text-slate-300">Description</label>
              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                rows={4}
                placeholder="Describe this lesson..."
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-cyan-400"
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">Order index</label>
              <input
                name="orderIndex"
                type="number"
                min="1"
                value={form.orderIndex}
                onChange={handleChange}
                placeholder="1"
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-cyan-400"
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">Duration</label>
              <input
                name="duration"
                type="number"
                min="1"
                value={form.duration}
                onChange={handleChange}
                placeholder="30"
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-cyan-400"
                required
              />
            </div>

            <div className="md:col-span-2">
              <label className="mb-2 block text-sm text-slate-300">Lecture text</label>
              <textarea
                name="lectureText"
                value={form.lectureText}
                onChange={handleChange}
                rows={8}
                placeholder="Write lesson notes or lecture content here..."
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-cyan-400"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">Video file</label>
              <input
                type="file"
                accept="video/*"
                onChange={handleVideoChange}
                className="block w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-slate-300 file:mr-4 file:rounded-xl file:border-0 file:bg-cyan-500 file:px-4 file:py-2 file:font-semibold file:text-white hover:file:bg-cyan-400"
              />
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">Lecture PDF</label>
              <input
                type="file"
                accept=".pdf,application/pdf"
                onChange={handlePdfChange}
                className="block w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-slate-300 file:mr-4 file:rounded-xl file:border-0 file:bg-cyan-500 file:px-4 file:py-2 file:font-semibold file:text-white hover:file:bg-cyan-400"
              />
            </div>
          </div>

          {error && (
            <div className="mt-5 rounded-2xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200">
              {error}
            </div>
          )}

          {success && (
            <div className="mt-5 rounded-2xl border border-emerald-400/20 bg-emerald-400/10 p-4 text-sm text-emerald-200">
              {success}
            </div>
          )}

          <div className="mt-6 flex flex-col gap-3 sm:flex-row">
            <button
              type="submit"
              disabled={loading}
              className="rounded-2xl bg-cyan-500 px-6 py-3 font-semibold text-white transition hover:bg-cyan-400 disabled:opacity-70"
            >
              {loading ? "Creating..." : "Create Lesson"}
            </button>

            <button
              type="button"
              onClick={() => navigate("/teacher/courses")}
              className="rounded-2xl border border-white/10 bg-white/5 px-6 py-3 font-semibold text-slate-200 transition hover:bg-white/10"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}