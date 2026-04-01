import { Link } from "react-router-dom";
import { ShieldAlert } from "lucide-react";

export default function UnauthorizedPage() {
  return (
    <div className="flex min-h-[calc(100vh-73px)] items-center justify-center px-6 py-10">
      <div className="w-full max-w-lg rounded-[32px] border border-white/10 bg-[#0d1b2a]/80 p-8 text-center shadow-[0_20px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl">
        <div className="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-3xl bg-red-400/10 text-red-300">
          <ShieldAlert size={28} />
        </div>

        <h1 className="text-3xl font-bold text-white">Access denied</h1>
        <p className="mt-3 text-slate-300">
          You do not have permission to open this page. Please log in with the correct account.
        </p>

        <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Link
            to="/login"
            className="rounded-2xl bg-blue-500 px-5 py-3 font-semibold text-white hover:bg-blue-400"
          >
            Go to Login
          </Link>

          <Link
            to="/"
            className="rounded-2xl border border-white/10 bg-white/5 px-5 py-3 font-semibold text-slate-200 hover:bg-white/10"
          >
            Back to Home
          </Link>
        </div>
      </div>
    </div>
  );
}