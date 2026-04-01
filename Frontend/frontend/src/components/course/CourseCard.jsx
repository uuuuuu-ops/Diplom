import { Clock3, Star, Layers3 } from "lucide-react";
import { Link } from "react-router-dom";

export default function CourseCard({
  id,
  title,
  category,
  level,
  price,
  badge,
  lessons,
  rating,
  duration,
}) {
  return (
    <div className="group overflow-hidden rounded-[28px] border border-white/10 bg-[#10233a] shadow-lg transition duration-300 hover:-translate-y-1 hover:border-blue-400/30 hover:shadow-2xl">
      <div className="relative h-48 overflow-hidden bg-gradient-to-br from-blue-500/30 via-cyan-400/20 to-transparent">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.12),transparent_35%)]" />
        <div className="absolute left-4 top-4 rounded-full bg-blue-400/15 px-3 py-1 text-xs font-semibold text-blue-200 backdrop-blur">
          {badge}
        </div>

        <div className="absolute bottom-4 left-4 right-4">
          <div className="mb-2 text-xs uppercase tracking-[0.2em] text-blue-200/80">
            {category}
          </div>
          <h3 className="text-2xl font-bold text-white">{title}</h3>
        </div>
      </div>

      <div className="p-6">
        <div className="mb-4 flex items-center justify-between text-sm">
          <span className="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-slate-300">
            {level}
          </span>
          <span className="text-lg font-bold text-white">{price}</span>
        </div>

        <div className="grid grid-cols-3 gap-3 text-sm text-slate-300">
          <div className="rounded-2xl border border-white/10 bg-white/5 p-3">
            <div className="mb-2 flex items-center gap-2 text-blue-300">
              <Layers3 size={16} />
              <span>Lessons</span>
            </div>
            <div className="font-semibold text-white">{lessons}</div>
          </div>

          <div className="rounded-2xl border border-white/10 bg-white/5 p-3">
            <div className="mb-2 flex items-center gap-2 text-yellow-300">
              <Star size={16} />
              <span>Rating</span>
            </div>
            <div className="font-semibold text-white">{rating}</div>
          </div>

          <div className="rounded-2xl border border-white/10 bg-white/5 p-3">
            <div className="mb-2 flex items-center gap-2 text-cyan-300">
              <Clock3 size={16} />
              <span>Duration</span>
            </div>
            <div className="font-semibold text-white">{duration}</div>
          </div>
        </div>

        <div className="mt-6 flex items-center justify-between">
          <p className="max-w-[70%] text-sm leading-6 text-slate-400">
            Learn with a premium interface, structured content, and real progress tracking.
          </p>

          <Link
            to={`/student/courses/${id}`}
            className="rounded-2xl bg-blue-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-400"
          >
            Open
          </Link>
        </div>
      </div>
    </div>
  );
}