import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthShell from "../../components/ui/AuthShell";
import { registerUser } from "../../services/authService";

export default function RegisterPage() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    role: "STUDENT",
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

      await registerUser({
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password,
        role: form.role,
      });

      navigate("/verify-email", {
        state: { email: form.email.trim() },
      });
    } catch (err) {
      console.error("Register failed:", err);

      const message =
        err?.response?.data?.message ||
        err?.response?.data ||
        "Registration failed.";

      setError(typeof message === "string" ? message : "Registration failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Create account"
      subtitle="Join the platform as a student or teacher."
      sideTitle="Start your journey in a cleaner, smarter LMS."
      sideText="Create an account, verify your email, and enter a modern platform built for learning, teaching, and growth."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="mb-2 block text-sm text-slate-300">Full name</label>
          <input
            name="name"
            type="text"
            value={form.name}
            onChange={handleChange}
            placeholder="Your full name"
            className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400"
            required
          />
        </div>

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
            placeholder="Create password"
            className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400"
            required
          />
        </div>

        <div>
          <label className="mb-2 block text-sm text-slate-300">Role</label>
          <select
            name="role"
            value={form.role}
            onChange={handleChange}
            className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none focus:border-blue-400"
          >
            <option value="STUDENT">Student</option>
            <option value="TEACHER">Teacher</option>
          </select>
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
          {loading ? "Creating account..." : "Register"}
        </button>
      </form>
    </AuthShell>
  );
}