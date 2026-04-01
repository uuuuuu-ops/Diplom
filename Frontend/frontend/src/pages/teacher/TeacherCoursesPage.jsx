import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { BookPlus, GraduationCap } from "lucide-react";
import { getMyCourses } from "../../services/courseService";
import TeacherCourseCard from "../../components/course/TeacherCourseCard";

export default function TeacherCoursesPage() {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadCourses() {
      try {
        setLoading(true);
        setError("");

        const data = await getMyCourses();
        setCourses(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error("Failed to load teacher courses:", err);

        const message =
          err?.response?.data?.message ||
          err?.response?.data ||
          "Failed to load your courses.";

        setError(typeof message === "string" ? message : "Failed to load your courses.");
      } finally {
        setLoading(false);
      }
    }

    loadCourses();
  }, []);

  const publishedCount = courses.filter((c) => c.published).length;
  const draftCount = courses.filter((c) => !c.published).length;

  return (
    <div className="px-6 py-10">
      <div className="mx-auto max-w-7xl">
        <div className="mb-10 flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="mb-3 text-sm uppercase tracking-[0.24em] text-blue-300">
              Teacher Dashboard
            </p>
            <h1 className="text-4xl font-bold text-white md:text-5xl">
              My Courses
            </h1>
            <p className="mt-4 max-w-2xl text-lg leading-8 text-slate-300">
              Manage your draft and published courses. Draft courses can be edited
              and expanded with lessons before publication.
            </p>
          </div>

          <Link
            to="/teacher/courses/new"
            className="inline-flex items-center justify-center gap-2 rounded-2xl bg-blue-500 px-6 py-4 font-semibold text-white transition hover:bg-blue-400"
          >
            <BookPlus size={20} />
            Create Course
          </Link>
        </div>

        <div className="mb-8 grid gap-4 md:grid-cols-3">
          <div className="rounded-[28px] border border-white/10 bg-white/5 p-6">
            <div className="text-sm text-slate-400">Total courses</div>
            <div className="mt-2 text-3xl font-bold text-white">{courses.length}</div>
          </div>

          <div className="rounded-[28px] border border-white/10 bg-white/5 p-6">
            <div className="text-sm text-slate-400">Published</div>
            <div className="mt-2 text-3xl font-bold text-emerald-300">{publishedCount}</div>
          </div>

          <div className="rounded-[28px] border border-white/10 bg-white/5 p-6">
            <div className="text-sm text-slate-400">Drafts</div>
            <div className="mt-2 text-3xl font-bold text-amber-300">{draftCount}</div>
          </div>
        </div>

        {loading && (
          <div className="rounded-2xl border border-white/10 bg-white/5 p-6 text-slate-300">
            Loading your courses...
          </div>
        )}

        {error && (
          <div className="rounded-2xl border border-red-400/20 bg-red-400/10 p-6 text-red-200">
            {error}
          </div>
        )}

        {!loading && !error && courses.length === 0 && (
          <div className="rounded-[32px] border border-white/10 bg-[#0d1b2a]/80 p-10 text-center">
            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-3xl bg-blue-500/15 text-blue-300">
              <GraduationCap size={28} />
            </div>

            <h2 className="text-2xl font-bold text-white">No courses yet</h2>
            <p className="mx-auto mt-3 max-w-xl text-slate-300">
              Start by creating your first course. Draft courses can be edited and
              filled with lessons before publishing.
            </p>

            <Link
              to="/teacher/courses/new"
              className="mt-6 inline-flex items-center gap-2 rounded-2xl bg-blue-500 px-5 py-3 font-semibold text-white transition hover:bg-blue-400"
            >
              <BookPlus size={18} />
              Create Your First Course
            </Link>
          </div>
        )}

        {!loading && !error && courses.length > 0 && (
          <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {courses.map((course) => (
              <TeacherCourseCard key={course.id} course={course} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}