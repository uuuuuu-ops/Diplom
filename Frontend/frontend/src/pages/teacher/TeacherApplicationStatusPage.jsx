import { useEffect, useState } from "react";
import { CheckCircle2, Clock3, XCircle, FileText } from "lucide-react";
import { Link } from "react-router-dom";
import { getMyTeacherApplication } from "../../services/teacherApplicationService";

function StatusBadge({ status }) {
  const normalized = String(status || "").toUpperCase();

  if (normalized === "APPROVED") {
    return (
      <div className="inline-flex items-center gap-2 rounded-full border border-emerald-400/20 bg-emerald-400/10 px-4 py-2 text-sm font-semibold text-emerald-300">
        <CheckCircle2 size={16} />
        Approved
      </div>
    );
  }

  if (normalized === "REJECTED") {
    return (
      <div className="inline-flex items-center gap-2 rounded-full border border-red-400/20 bg-red-400/10 px-4 py-2 text-sm font-semibold text-red-300">
        <XCircle size={16} />
        Rejected
      </div>
    );
  }

  return (
    <div className="inline-flex items-center gap-2 rounded-full border border-amber-400/20 bg-amber-400/10 px-4 py-2 text-sm font-semibold text-amber-300">
      <Clock3 size={16} />
      Pending Review
    </div>
  );
}

export default function TeacherApplicationStatusPage() {
  const [application, setApplication] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadStatus() {
      try {
        setLoading(true);
        setError("");

        const data = await getMyTeacherApplication();
        setApplication(data);
      } catch (err) {
        console.error("Failed to load teacher application:", err);

        const message =
          err?.response?.data?.message ||
          err?.response?.data ||
          "Could not load application status.";

        setError(typeof message === "string" ? message : "Could not load application status.");
      } finally {
        setLoading(false);
      }
    }

    loadStatus();
  }, []);

  if (loading) {
    return (
      <div className="px-6 py-10">
        <div className="mx-auto max-w-4xl rounded-[28px] border border-white/10 bg-white/5 p-6 text-slate-300">
          Loading application status...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="px-6 py-10">
        <div className="mx-auto max-w-4xl rounded-[28px] border border-red-400/20 bg-red-400/10 p-6 text-red-200">
          <div className="text-xl font-semibold">Application status unavailable</div>
          <p className="mt-2">{error}</p>

          <Link
            to="/teacher/apply"
            className="mt-5 inline-flex rounded-2xl bg-blue-500 px-4 py-3 font-semibold text-white hover:bg-blue-400"
          >
            Open application form
          </Link>
        </div>
      </div>
    );
  }

  const status = String(application?.status || "PENDING").toUpperCase();

  return (
    <div className="px-6 py-10">
      <div className="mx-auto max-w-4xl">
        <div className="mb-8">
          <div className="mb-4 inline-flex rounded-full border border-blue-400/20 bg-blue-400/10 px-4 py-2 text-sm font-medium text-blue-200">
            Teacher Application
          </div>

          <h1 className="text-4xl font-bold text-white">Application Status</h1>
          <p className="mt-3 max-w-2xl text-lg leading-8 text-slate-300">
            Track your instructor approval progress and review details.
          </p>
        </div>

        <div className="rounded-[32px] border border-white/10 bg-[#0d1b2a]/80 p-8 shadow-[0_20px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <div className="text-sm text-slate-400">Current status</div>
              <div className="mt-2">
                <StatusBadge status={status} />
              </div>
            </div>

            {status === "APPROVED" && (
              <Link
                to="/teacher/courses"
                className="rounded-2xl bg-blue-500 px-5 py-3 font-semibold text-white hover:bg-blue-400"
              >
                Go to Teacher Dashboard
              </Link>
            )}
          </div>

          <div className="mt-8 grid gap-5 md:grid-cols-2">
            <div className="rounded-2xl border border-white/10 bg-[#081120] p-5">
              <div className="text-sm text-slate-400">Full name</div>
              <div className="mt-2 text-lg font-semibold text-white">
                {application.fullName || "—"}
              </div>
            </div>

            <div className="rounded-2xl border border-white/10 bg-[#081120] p-5">
              <div className="text-sm text-slate-400">Email</div>
              <div className="mt-2 text-lg font-semibold text-white">
                {application.email || "—"}
              </div>
            </div>

            <div className="rounded-2xl border border-white/10 bg-[#081120] p-5">
              <div className="text-sm text-slate-400">Specialization</div>
              <div className="mt-2 text-lg font-semibold text-white">
                {application.specialization || "—"}
              </div>
            </div>

            <div className="rounded-2xl border border-white/10 bg-[#081120] p-5">
              <div className="text-sm text-slate-400">Years of experience</div>
              <div className="mt-2 text-lg font-semibold text-white">
                {application.yearsOfExperience ?? "—"}
              </div>
            </div>
          </div>

          <div className="mt-5 rounded-2xl border border-white/10 bg-[#081120] p-5">
            <div className="mb-2 text-sm text-slate-400">Resume</div>
            {application.id ? (
              <a
                href={`http://localhost:8080/teacher-applications/${application.id}/file`}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-2 font-semibold text-blue-300 hover:text-blue-200"
              >
                <FileText size={18} />
                Open resume PDF
              </a>
            ) : (
              <div className="text-white">Not available</div>
            )}
          </div>

          <div className="mt-5 rounded-2xl border border-white/10 bg-[#081120] p-5">
            <div className="text-sm text-slate-400">Review comment</div>
            <div className="mt-2 text-white">
              {application.reviewComment || "No review comment yet."}
            </div>
          </div>

          {status === "PENDING" && (
            <div className="mt-5 rounded-2xl border border-amber-400/20 bg-amber-400/10 p-5 text-amber-200">
              Your application is being reviewed by an administrator. Please wait for approval.
            </div>
          )}

          {status === "REJECTED" && (
            <div className="mt-5 rounded-2xl border border-red-400/20 bg-red-400/10 p-5 text-red-200">
              Your application was rejected. Review the comment above and improve your application.
            </div>
          )}

          {status === "APPROVED" && (
            <div className="mt-5 rounded-2xl border border-emerald-400/20 bg-emerald-400/10 p-5 text-emerald-200">
              Congratulations. Your instructor account has been approved.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}