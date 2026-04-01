import { Link } from "react-router-dom";
import { BookOpen, Pencil, PlusCircle, Globe, Lock } from "lucide-react";

export default function TeacherCourseCard({ course }) {
  const isPublished = Boolean(course.published);

  const priceLabel = course.free
    ? "Free"
    : `${course.price ?? ""} ${course.currency ?? ""}`.trim() || "Paid";

  return (
    <div className="overflow-hidden rounded-[28px] border border-white/10 bg-[#10233a] shadow-lg">
      <div className="relative h-44 overflow-hidden bg-gradient-to-br from-blue-500/30 via-cyan-400/20 to-transparent">
        {course.thumbnail && (
          <img
            src={
              course.thumbnail.startsWith("http")
                ? course.thumbnail
                : `http://localhost:8080${course.thumbnail}`
            }
            alt={course.title}
            className="h-full w-full object-cover"
          />
        )}

        <div className="absolute left-4 top-4 flex gap-2">
          <span
            className={`rounded-full px-3 py-1 text-xs font-semibold ${
              isPublished
                ? "bg-emerald-400/15 text-emerald-300"
                : "bg-amber-400/15 text-amber-300"
            }`}
          >
            {isPublished ? "Published" : "Draft"}
          </span>

          <span className="rounded-full bg-blue-400/15 px-3 py-1 text-xs font-semibold text-blue-200">
            {course.level || "Unknown"}
          </span>
        </div>
      </div>

      <div className="p-6">
        <div className="mb-2 text-sm text-slate-400">{course.category || "General"}</div>
        <h3 className="text-2xl font-bold text-white">{course.title}</h3>

        <p className="mt-3 line-clamp-3 min-h-[72px] text-slate-300">
          {course.description || "No description provided."}
        </p>

        <div className="mt-5 flex items-center justify-between text-sm text-slate-400">
          <span>{priceLabel}</span>
          <span>{isPublished ? "Visible to students" : "Not published yet"}</span>
        </div>

        <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-3">
          <Link
            to={`/student/courses/${course.id}`}
            className="inline-flex items-center justify-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-4 py-3 font-semibold text-slate-200 transition hover:bg-white/10"
          >
            <BookOpen size={18} />
            Open
          </Link>

          {!isPublished ? (
            <Link
              to={`/teacher/courses/${course.id}/edit`}
              className="inline-flex items-center justify-center gap-2 rounded-2xl bg-blue-500 px-4 py-3 font-semibold text-white transition hover:bg-blue-400"
            >
              <Pencil size={18} />
              Edit
            </Link>
          ) : (
            <div className="inline-flex items-center justify-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-4 py-3 font-semibold text-slate-500">
              <Lock size={18} />
              Locked
            </div>
          )}

          {!isPublished ? (
            <Link
              to={`/teacher/courses/${course.id}/lessons/new`}
              className="inline-flex items-center justify-center gap-2 rounded-2xl border border-cyan-400/20 bg-cyan-400/10 px-4 py-3 font-semibold text-cyan-200 transition hover:bg-cyan-400/15"
            >
              <PlusCircle size={18} />
              Lesson
            </Link>
          ) : (
            <div className="inline-flex items-center justify-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-4 py-3 font-semibold text-slate-500">
              <Globe size={18} />
              Published
            </div>
          )}
        </div>
      </div>
    </div>
  );
}