import { useEffect, useState } from "react";
import { CheckCircle2, PlayCircle, Lock, FileText, Star } from "lucide-react";
import { useParams } from "react-router-dom";
import { getCourseById } from "../../services/courseService";

const lessons = [
  { id: 1, title: "Introduction to the course", duration: "12 min", free: true },
  { id: 2, title: "Spring Boot project setup", duration: "24 min", free: true },
  { id: 3, title: "Controllers and routing", duration: "31 min", free: false },
  { id: 4, title: "Service layer and business logic", duration: "28 min", free: false },
  { id: 5, title: "Quiz and knowledge check", duration: "15 min", free: false },
];

export default function StudentCourseDetailsPage() {
  const { courseId } = useParams();
  const [course, setCourse] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadCourse() {
      try {
        setLoading(true);
        setError("");

        const data = await getCourseById(courseId);
        setCourse(data);
      } catch (err) {
        console.error("Failed to load course:", err);
        setError("Failed to load course");
      } finally {
        setLoading(false);
      }
    }

    loadCourse();
  }, [courseId]);

  if (loading) {
    return (
      <div className="px-6 py-10">
        <div className="mx-auto max-w-7xl rounded-2xl border border-white/10 bg-white/5 p-6 text-slate-300">
          Loading course...
        </div>
      </div>
    );
  }

  if (error || !course) {
    return (
      <div className="px-6 py-10">
        <div className="mx-auto max-w-7xl rounded-2xl border border-red-400/20 bg-red-400/10 p-6 text-red-200">
          {error || "Course not found"}
        </div>
      </div>
    );
  }

  return (
    <div className="px-6 py-10">
      <div className="mx-auto grid max-w-7xl gap-8 lg:grid-cols-[1.5fr_420px]">
        <div>
          <div className="mb-6 rounded-[32px] border border-white/10 bg-gradient-to-br from-blue-500/20 via-cyan-400/10 to-transparent p-8">
            <div className="mb-4 inline-flex rounded-full bg-blue-400/15 px-4 py-2 text-sm font-semibold text-blue-200">
              {course.category || "General"}
            </div>

            <h1 className="text-4xl font-bold text-white md:text-5xl">
              {course.title}
            </h1>

            <p className="mt-4 max-w-3xl text-lg leading-8 text-slate-300">
              {course.description || "No description available."}
            </p>

            <div className="mt-6 flex flex-wrap gap-6 text-sm text-slate-300">
              <div className="flex items-center gap-2">
                <Star size={16} className="text-yellow-300" />
                4.8 rating
              </div>
              <div>{course.level || "Unknown"} level</div>
              <div>{course.free ? "Free course" : "Paid course"}</div>
              <div>{course.currency || "USD"}</div>
            </div>
          </div>

          <div className="rounded-[28px] border border-white/10 bg-[#0d1b2a]/80 p-6">
            <h2 className="text-2xl font-bold text-white">What you will learn</h2>

            <div className="mt-6 grid gap-4 md:grid-cols-2">
              {[
                "Understand the full learning flow",
                "Study structured lessons and resources",
                "Practice with quizzes and exercises",
                "Track progress across the course",
                "Access course materials in one place",
                "Prepare for certification and completion",
              ].map((item) => (
                <div
                  key={item}
                  className="flex items-start gap-3 rounded-2xl border border-white/10 bg-white/5 p-4"
                >
                  <CheckCircle2 size={18} className="mt-1 text-emerald-300" />
                  <span className="text-slate-200">{item}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="mt-8 rounded-[28px] border border-white/10 bg-[#0d1b2a]/80 p-6">
            <h2 className="text-2xl font-bold text-white">Course content</h2>
            <p className="mt-2 text-slate-400">Structured lessons with locked and free previews.</p>

            <div className="mt-6 space-y-4">
              {lessons.map((lesson) => (
                <div
                  key={lesson.id}
                  className="flex items-center justify-between rounded-2xl border border-white/10 bg-white/5 p-4"
                >
                  <div className="flex items-center gap-4">
                    <div className="rounded-2xl bg-blue-500/15 p-3 text-blue-300">
                      {lesson.free ? <PlayCircle size={20} /> : <Lock size={20} />}
                    </div>

                    <div>
                      <div className="font-semibold text-white">{lesson.title}</div>
                      <div className="text-sm text-slate-400">{lesson.duration}</div>
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    {lesson.free && (
                      <span className="rounded-full bg-emerald-400/10 px-3 py-1 text-xs font-semibold text-emerald-300">
                        Preview
                      </span>
                    )}

                    <button className="rounded-xl border border-white/10 px-4 py-2 text-sm text-slate-200 hover:bg-white/5">
                      Open
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <aside className="h-fit rounded-[28px] border border-white/10 bg-[#10233a] p-6 shadow-2xl">
          <div className="h-52 overflow-hidden rounded-[24px] bg-gradient-to-br from-blue-500/30 via-cyan-400/20 to-transparent">
            {course.thumbnail ? (
              <img
                src={course.thumbnail.startsWith("http") ? course.thumbnail : `http://localhost:8080${course.thumbnail}`}
                alt={course.title}
                className="h-full w-full object-cover"
              />
            ) : null}
          </div>

          <div className="mt-6">
            <div className="text-sm text-slate-400">Price</div>
            <div className="mt-1 text-4xl font-bold text-white">
              {course.free
                ? "Free"
                : `${course.price ?? ""} ${course.currency ?? ""}`.trim() || "Paid"}
            </div>

            <div className="mt-6 space-y-3">
              <button className="w-full rounded-2xl bg-blue-500 px-4 py-3 font-semibold text-white transition hover:bg-blue-400">
                {course.free ? "Enroll for Free" : "Buy Course"}
              </button>

              {!course.free && (
                <button className="w-full rounded-2xl border border-white/10 bg-white/5 px-4 py-3 font-semibold text-slate-200 transition hover:bg-white/10">
                  Subscribe for Access
                </button>
              )}
            </div>

            <div className="mt-6 space-y-4 text-sm text-slate-300">
              <div className="flex items-center justify-between">
                <span>Access type</span>
                <span className="text-white">{course.free ? "Free" : "Paid / Subscription"}</span>
              </div>
              <div className="flex items-center justify-between">
                <span>Resources</span>
                <span className="inline-flex items-center gap-2 text-white">
                  <FileText size={16} />
                  Files included
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span>Certificate</span>
                <span className="text-white">Available</span>
              </div>
              <div className="flex items-center justify-between">
                <span>Level</span>
                <span className="text-white">{course.level || "Unknown"}</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}