import { useState } from "react";
import api from "../../services/api";

export default function TeacherApplyPage() {
  const [form, setForm] = useState({
    fullName: "",
    email: localStorage.getItem("email") || "",
    specialization: "",
    yearsOfExperience: "",
  });

  const [resumeFile, setResumeFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function handleFileChange(e) {
    setResumeFile(e.target.files[0] || null);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    console.log("SUBMIT START");

    try {
      setLoading(true);
      setError("");
      setSuccess("");

      if (!resumeFile) {
        throw new Error("Please upload your resume PDF.");
      }

      const formData = new FormData();
      formData.append("fullName", form.fullName.trim());
      formData.append("email", form.email.trim());
      formData.append("specialization", form.specialization.trim());
      formData.append("yearsOfExperience", form.yearsOfExperience);
      formData.append("resumeFile", resumeFile);

      const response = await api.post("/teacher-applications", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      console.log("SUBMIT RESPONSE:", response.data);
      setSuccess("Application submitted successfully.");

      setTimeout(() => {
      window.location.href = "/teacher/application-status";
      }, 800);
    } catch (err) {
      console.error("Teacher application failed:", err);
      console.error("Teacher application response:", err?.response);

      const message =
        err?.response?.data?.message ||
        err?.response?.data ||
        err?.message ||
        "Failed to submit application.";

      setError(typeof message === "string" ? message : "Failed to submit application.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-[calc(100vh-73px)] px-6 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white">Become a Teacher</h1>
          <p className="mt-2 text-slate-400">
            Submit your application to start creating courses.
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="space-y-5 rounded-[28px] border border-white/10 bg-[#0d1b2a]/80 p-8 backdrop-blur"
        >
          <div>
            <label className="mb-2 block text-sm text-slate-300">
              Full name
            </label>
            <input
              name="fullName"
              value={form.fullName}
              onChange={handleChange}
              className="w-full rounded-xl bg-[#081120] p-3 text-white outline-none"
              required
            />
          </div>

          <div>
            <label className="mb-2 block text-sm text-slate-300">
              Email
            </label>
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              className="w-full rounded-xl bg-[#081120] p-3 text-white outline-none"
              required
            />
          </div>

          <div>
            <label className="mb-2 block text-sm text-slate-300">
              Specialization
            </label>
            <input
              name="specialization"
              value={form.specialization}
              onChange={handleChange}
              placeholder="Backend, Frontend, AI..."
              className="w-full rounded-xl bg-[#081120] p-3 text-white outline-none"
              required
            />
          </div>

          <div>
            <label className="mb-2 block text-sm text-slate-300">
              Years of experience
            </label>
            <input
              name="yearsOfExperience"
              value={form.yearsOfExperience}
              onChange={handleChange}
              type="number"
              min="0"
              className="w-full rounded-xl bg-[#081120] p-3 text-white outline-none"
              required
            />
          </div>

          <div>
            <label className="mb-2 block text-sm text-slate-300">
              Resume (PDF)
            </label>
            <input
              type="file"
              accept=".pdf,application/pdf"
              onChange={handleFileChange}
              className="w-full text-sm text-slate-300"
              required
            />
          </div>

          {error && <div className="text-red-400">{error}</div>}
          {success && <div className="text-green-400">{success}</div>}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-xl bg-blue-500 p-3 font-semibold text-white hover:bg-blue-400 disabled:opacity-70"
          >
            {loading ? "Submitting..." : "Submit Application"}
          </button>
        </form>
      </div>
    </div>
  );
}