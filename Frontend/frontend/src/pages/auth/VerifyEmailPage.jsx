import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import AuthShell from "../../components/ui/AuthShell";
import { verifyEmail } from "../../services/authService";

export default function VerifyEmailPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const [form, setForm] = useState({
    email: location.state?.email || "",
    code: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      setLoading(true);
      setError("");
      setSuccess("");

      await verifyEmail({
        email: form.email.trim(),
        code: form.code.trim(),
      });

      setSuccess("Email verified successfully.");
      setTimeout(() => navigate("/login"), 900);
    } catch (err) {
      console.error("Verify failed:", err);

      const message =
        err?.response?.data?.message ||
        err?.response?.data ||
        "Verification failed.";

      setError(typeof message === "string" ? message : "Verification failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthShell
      title="Verify email"
      subtitle="Enter the verification code sent to your email."
      sideTitle="One last step before you enter the platform."
      sideText="Verify your account to unlock the full LMS experience and continue with login."
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
          <label className="mb-2 block text-sm text-slate-300">Verification code</label>
          <input
            name="code"
            type="text"
            value={form.code}
            onChange={handleChange}
            placeholder="123456"
            className="w-full rounded-2xl border border-white/10 bg-[#081120] px-4 py-3 text-white outline-none placeholder:text-slate-500 focus:border-blue-400"
            required
          />
        </div>

        {error && (
          <div className="rounded-2xl border border-red-400/20 bg-red-400/10 p-4 text-sm text-red-200">
            {error}
          </div>
        )}

        {success && (
          <div className="rounded-2xl border border-emerald-400/20 bg-emerald-400/10 p-4 text-sm text-emerald-200">
            {success}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-2xl bg-blue-500 px-4 py-3 font-semibold text-white transition hover:bg-blue-400 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {loading ? "Verifying..." : "Verify"}
        </button>
      </form>
    </AuthShell>
  );
}