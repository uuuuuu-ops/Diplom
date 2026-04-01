import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthShell from "../../components/ui/AuthShell";
import { loginUser } from "../../services/authService";
import { saveAuth } from "../../lib/auth";

function normalizeRole(role) {
  if (!role) return "";
  return String(role).replace("ROLE_", "").trim().toUpperCase();
}

function getRedirectByRole(role, teacherApproved) {
  const normalizedRole = normalizeRole(role);

  if (normalizedRole === "ADMIN") return "/admin/applications";

  if (normalizedRole === "TEACHER") {
    if (typeof teacherApproved === "undefined") {
      return "/teacher/apply";
    }
    return teacherApproved ? "/teacher/courses" : "/teacher-applications/my";
  }

  return "/student/courses";
}

export default function LoginPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      setLoading(true);
      setError("");

      const data = await loginUser({
        email: form.email.trim(),
        password: form.password,
      });
      console.log("LOGIN RESPONSE:", data);

      saveAuth(data);

      const redirectPath = getRedirectByRole(data.role, data.teacherApproved);
      navigate(redirectPath);
    } catch (err) {
      console.error("Login failed:", err);

      const message =
        err?.response?.data?.message ||
        err?.response?.data ||
        "Login failed. Check email and password.";

      setError(typeof message === "string" ? message : "Login failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Sign in to continue learning."
      sideTitle="Return to your courses, progress, and goals."
      sideText="Log in and continue your learning journey with a darker, cleaner, more focused experience."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-2 block text-sm text-slate-300">Email</label>
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            placeholder="you@example.com"
            className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400"
            required
          />
        </div>

        <div>
          <label className="mb-2 block text-sm text-slate-300">Password</label>
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            placeholder="Enter password"
            className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400"
            required
          />
        </div>

        {error && (
          <div className="rounded-2xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-2xl bg-blue-500 px-4 py-3 font-semibold text-white transition hover:bg-blue-400 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
    </AuthShell>
  );
}