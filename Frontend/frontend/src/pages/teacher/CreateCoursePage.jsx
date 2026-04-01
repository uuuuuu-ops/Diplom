import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createCourse } from "../../services/courseService";

export default function CreateCoursePage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    title: "",
    description: "",
    category: "",
    level: "BEGINNER",
    free: true,
    price: "",
    published: false,
  });

  const [thumbnailFile, setThumbnailFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function handleChange(e) {
    const { name, value, type, checked } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  }

  function handleFreeChange(value) {
    setForm((prev) => ({
      ...prev,
      free: value,
      price: value ? "" : prev.price,
    }));
  }

  function handleFileChange(e) {
    setThumbnailFile(e.target.files[0] || null);
  }

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      setLoading(true);
      setError("");

      if (!form.free && !form.price) {
        throw new Error("Price is required for paid course.");
      }

      const formData = new FormData();
      formData.append("title", form.title.trim());
      formData.append("description", form.description.trim());
      formData.append("category", form.category.trim());
      formData.append("level", form.level);
      formData.append("free", String(form.free));
      formData.append("published", String(form.published));

      if (!form.free) {
        formData.append("price", form.price);
      }

      if (thumbnailFile) {
        formData.append("thumbnailFile", thumbnailFile);
      }

      const createdCourse = await createCourse(formData);
      navigate("/teacher/courses");
    } catch (err) {
      console.error("Create course failed:", err);

      const message =
        err?.response?.data?.message ||
        err?.response?.data ||
        err?.message ||
        "Failed to create course.";

      setError(typeof message === "string" ? message : "Failed to create course.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="px-6 py-10">
      <div className="mx-auto max-w-4xl">
        <div className="mb-8">
          <p className="mb-3 text-sm uppercase tracking-[0.24em] text-blue-300">
            Teacher Dashboard
          </p>
          <h1 className="text-4xl font-bold text-white">Create Course</h1>
          <p className="mt-3 max-w-2xl text-lg leading-8 text-slate-300">
            Build a new draft course. You can keep it unpublished while adding lessons.
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="rounded-[32px] border border-white/10 bg-[#0d1b2a]/80 p-8 shadow-[0_20px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl"
        >
          <div className="grid gap-5 md:grid-cols-2">
            <div className="md:col-span-2">
              <label className="mb-2 block text-sm text-slate-300">Title</label>
              <input
                name="title"
                value={form.title}
                onChange={handleChange}
                placeholder="Spring Boot Basics"
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400"
                required
              />
            </div>

            <div className="md:col-span-2">
              <label className="mb-2 block text-sm text-slate-300">Description</label>
              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                rows={5}
                placeholder="Describe what students will learn..."
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400"
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">Category</label>
              <input
                name="category"
                value={form.category}
                onChange={handleChange}
                placeholder="Backend Development"
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400"
                required
              />
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">Level</label>
              <select
                name="level"
                value={form.level}
                onChange={handleChange}
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none focus:border-blue-400"
              >
                <option value="BEGINNER">BEGINNER</option>
                <option value="INTERMEDIATE">INTERMEDIATE</option>
                <option value="ADVANCED">ADVANCED</option>
              </select>
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">Access Type</label>
              <div className="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  onClick={() => handleFreeChange(true)}
                  className={`rounded-2xl px-4 py-3 font-semibold transition ${
                    form.free
                      ? "bg-emerald-500 text-white"
                      : "border border-white/10 bg-[#081120] text-slate-300"
                  }`}
                >
                  Free
                </button>

                <button
                  type="button"
                  onClick={() => handleFreeChange(false)}
                  className={`rounded-2xl px-4 py-3 font-semibold transition ${
                    !form.free
                      ? "bg-blue-500 text-white"
                      : "border border-white/10 bg-[#081120] text-slate-300"
                  }`}
                >
                  Paid
                </button>
              </div>
            </div>

            <div>
              <label className="mb-2 block text-sm text-slate-300">Price (USD)</label>
              <input
                name="price"
                type="number"
                min="0"
                step="0.01"
                value={form.price}
                onChange={handleChange}
                disabled={form.free}
                placeholder="19.99"
                className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400 disabled:opacity-50"
              />
            </div>

            <div className="md:col-span-2">
              <label className="mb-2 block text-sm text-slate-300">Thumbnail</label>
              <input
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                className="block w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-slate-300 file:mr-4 file:rounded-xl file:border-0 file:bg-blue-500 file:px-4 file:py-2 file:font-semibold file:text-white hover:file:bg-blue-400"
              />
            </div>

            <div className="md:col-span-2">
              <label className="flex items-center gap-3 rounded-2xl border border-white/10 bg-[#081120] px-4 py-4 text-slate-300">
                <input
                  type="checkbox"
                  name="published"
                  checked={form.published}
                  onChange={handleChange}
                />
                Publish immediately
              </label>
            </div>
          </div>

          {error && (
            <div className="mt-5 rounded-2xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200">
              {error}
            </div>
          )}

          <div className="mt-6 flex flex-col gap-3 sm:flex-row">
            <button
              type="submit"
              disabled={loading}
              className="rounded-2xl bg-blue-500 px-6 py-3 font-semibold text-white transition hover:bg-blue-400 disabled:opacity-70"
            >
              {loading ? "Creating..." : "Create Course"}
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