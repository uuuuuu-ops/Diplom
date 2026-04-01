import { useEffect, useMemo, useState } from "react";
import { Search, SlidersHorizontal } from "lucide-react";
import CourseCard from "../../components/course/CourseCard";
import { getPublicCourses } from "../../services/courseService";

export default function StudentCoursesPage() {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("All Categories");
  const [level, setLevel] = useState("All Levels");
  const [tag, setTag] = useState("All");

  useEffect(() => {
    async function loadCourses() {
      try {
        setLoading(true);
        setError("");

        const data = await getPublicCourses();
        setCourses(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error("Failed to load courses:", err);
        setError("Failed to load courses");
      } finally {
        setLoading(false);
      }
    }

    loadCourses();
  }, []);

  const categories = useMemo(() => {
    const values = [...new Set(courses.map((c) => c.category).filter(Boolean))];
    return ["All Categories", ...values];
  }, [courses]);

  const levels = useMemo(() => {
    const values = [...new Set(courses.map((c) => c.level).filter(Boolean))];
    return ["All Levels", ...values];
  }, [courses]);

  const filteredCourses = useMemo(() => {
    return courses.filter((course) => {
      const title = course.title?.toLowerCase() ?? "";
      const description = course.description?.toLowerCase() ?? "";
      const categoryValue = course.category ?? "";
      const levelValue = course.level ?? "";

      const matchesSearch =
        title.includes(search.toLowerCase()) ||
        description.includes(search.toLowerCase());

      const matchesCategory =
        category === "All Categories" || categoryValue === category;

      const matchesLevel =
        level === "All Levels" || levelValue === level;

      const matchesTag =
        tag === "All" ||
        (tag === "Free" && course.free) ||
        (tag === "Paid" && !course.free) ||
        (tag === "Popular") ||
        (tag === "New") ||
        (tag === "Subscription");

      return matchesSearch && matchesCategory && matchesLevel && matchesTag;
    });
  }, [courses, search, category, level, tag]);

  return (
    <div className="px-6 py-10">
      <div className="mx-auto max-w-7xl">
        <div className="mb-10">
          <p className="mb-3 text-sm uppercase tracking-[0.24em] text-blue-300">
            Course Catalog
          </p>

          <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
            <div>
              <h1 className="text-4xl font-bold text-white md:text-5xl">
                Discover your next learning path
              </h1>
              <p className="mt-4 max-w-2xl text-lg leading-8 text-slate-300">
                Browse premium courses in one modern LMS experience.
              </p>
            </div>

            <div className="rounded-3xl border border-white/10 bg-white/5 px-5 py-4">
              <div className="text-sm text-slate-400">Available now</div>
              <div className="mt-1 text-3xl font-bold text-white">
                {filteredCourses.length}
              </div>
            </div>
          </div>
        </div>

        <div className="mb-8 grid gap-4 lg:grid-cols-[1.5fr_1fr_1fr_auto]">
          <div className="flex items-center gap-3 rounded-2xl border border-white/10 bg-[#0d1b2a] px-4 py-3">
            <Search size={18} className="text-slate-400" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search courses, topics, technologies..."
              className="w-full bg-transparent text-white outline-none placeholder:text-slate-500"
            />
          </div>

          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="rounded-2xl border border-white/10 bg-[#0d1b2a] px-4 py-3 text-white outline-none"
          >
            {categories.map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>

          <select
            value={level}
            onChange={(e) => setLevel(e.target.value)}
            className="rounded-2xl border border-white/10 bg-[#0d1b2a] px-4 py-3 text-white outline-none"
          >
            {levels.map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>

          <button className="inline-flex items-center justify-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-5 py-3 font-semibold text-slate-200 transition hover:bg-white/10">
            <SlidersHorizontal size={18} />
            Filters
          </button>
        </div>

        <div className="mb-8 flex flex-wrap gap-3">
          {["All", "Free", "Paid", "Subscription", "Popular", "New"].map((item) => (
            <button
              key={item}
              onClick={() => setTag(item)}
              className={`rounded-full px-4 py-2 text-sm font-medium transition ${
                tag === item
                  ? "bg-blue-500 text-white"
                  : "border border-white/10 bg-white/5 text-slate-300 hover:bg-white/10"
              }`}
            >
              {item}
            </button>
          ))}
        </div>

        {loading && (
          <div className="rounded-2xl border border-white/10 bg-white/5 p-5 text-lg text-slate-300">
            Loading courses...
          </div>
        )}

        {error && (
          <div className="rounded-2xl border border-red-400/20 bg-red-400/10 p-4 text-red-200">
            {error}
          </div>
        )}

        {!loading && !error && filteredCourses.length === 0 && (
          <div className="rounded-2xl border border-white/10 bg-white/5 p-6 text-slate-300">
            No courses found.
          </div>
        )}

        {!loading && !error && filteredCourses.length > 0 && (
          <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
            {filteredCourses.map((course) => (
              <CourseCard
                key={course.id}
                id={course.id}
                title={course.title}
                category={course.category || "General"}
                level={course.level || "Unknown"}
                price={
                  course.free
                    ? "Free"
                    : `${course.price ?? ""} ${course.currency ?? ""}`.trim() || "Paid"
                }
                badge={course.free ? "Free" : "Premium"}
                lessons={course.lessonCount ?? 0}
                rating={course.rating ?? "4.8"}
                duration={course.duration ?? "—"}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}