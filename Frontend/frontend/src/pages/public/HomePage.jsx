import { ArrowRight, PlayCircle, ShieldCheck, BookOpen, Users, Sparkles } from "lucide-react";

function Header() {
  return (
    <header className="sticky top-0 z-50 border-b border-white/10 bg-[#081120]/80 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
        <div className="text-xl font-bold tracking-wide text-white">Diplom LMS</div>

        <nav className="hidden items-center gap-8 text-sm text-slate-300 md:flex">
          <a href="#features" className="transition hover:text-white">Features</a>
          <a href="#roles" className="transition hover:text-white">Roles</a>
          <a href="#courses" className="transition hover:text-white">Courses</a>
        </nav>

        <div className="flex items-center gap-3">
          <a
            href="/login"
            className="rounded-xl border border-white/10 px-4 py-2 text-sm text-slate-300 transition hover:bg-white/5 hover:text-white"
          >
            Login
          </a>
          <a
            href="/register"
            className="rounded-xl bg-blue-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-blue-400"
          >
            Get Started
          </a>
        </div>
      </div>
    </header>
  );
}

function Hero() {
  return (
    <section className="relative overflow-hidden px-6 pb-20 pt-16 md:pb-28 md:pt-24">
      <div className="absolute inset-0 -z-10">
        <div className="absolute left-[10%] top-10 h-64 w-64 rounded-full bg-blue-500/20 blur-3xl" />
        <div className="absolute right-[10%] top-24 h-72 w-72 rounded-full bg-cyan-400/10 blur-3xl" />
      </div>

      <div className="mx-auto grid max-w-7xl items-center gap-12 md:grid-cols-2">
        <div>
          <div className="mb-4 inline-flex items-center gap-2 rounded-full border border-blue-400/20 bg-blue-400/10 px-4 py-2 text-sm text-blue-200">
            <Sparkles size={16} />
            Modern learning platform for your diploma project
          </div>

          <h1 className="max-w-2xl text-5xl font-extrabold leading-tight text-white md:text-7xl">
            Learn in a
            <span className="block bg-gradient-to-r from-blue-400 to-cyan-300 bg-clip-text text-transparent">
              deep ocean UI
            </span>
          </h1>

          <p className="mt-6 max-w-xl text-lg leading-8 text-slate-300">
            A premium LMS for students, teachers, and administrators with courses,
            lessons, quizzes, subscriptions, payments, and certificates.
          </p>

          <div className="mt-8 flex flex-col gap-4 sm:flex-row">
            <a
              href="/register"
              className="inline-flex items-center justify-center gap-2 rounded-2xl bg-blue-500 px-6 py-4 font-semibold text-white transition hover:bg-blue-400"
            >
              Start Learning
              <ArrowRight size={18} />
            </a>

            <a
              href="#features"
              className="inline-flex items-center justify-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-6 py-4 font-semibold text-slate-200 transition hover:bg-white/10"
            >
              <PlayCircle size={18} />
              Explore Platform
            </a>
          </div>

          <div className="mt-10 flex flex-wrap gap-6 text-sm text-slate-400">
            <div>JWT Auth</div>
            <div>Course Management</div>
            <div>Quizzes</div>
            <div>PayPal</div>
            <div>Certificates</div>
          </div>
        </div>

        <div className="relative">
          <div className="rounded-[28px] border border-white/10 bg-white/5 p-4 shadow-2xl backdrop-blur">
            <div className="rounded-[24px] bg-[#0d1b2a] p-6">
              <div className="mb-6 flex items-center justify-between">
                <div>
                  <p className="text-sm text-slate-400">Student Dashboard</p>
                  <h3 className="text-xl font-bold text-white">My Learning</h3>
                </div>
                <div className="rounded-xl border border-emerald-400/20 bg-emerald-400/10 px-3 py-2 text-sm text-emerald-300">
                  Active
                </div>
              </div>

              <div className="space-y-4">
                {[
                  { title: "Spring Boot Basics", progress: "78%" },
                  { title: "MongoDB for LMS", progress: "51%" },
                  { title: "Frontend Architecture", progress: "33%" },
                ].map((course) => (
                  <div
                    key={course.title}
                    className="rounded-2xl border border-white/10 bg-slate-900/40 p-4"
                  >
                    <div className="mb-3 flex items-center justify-between">
                      <div>
                        <div className="font-semibold text-white">{course.title}</div>
                        <div className="text-sm text-slate-400">Course progress</div>
                      </div>
                      <div className="text-sm font-semibold text-blue-300">{course.progress}</div>
                    </div>

                    <div className="h-2 rounded-full bg-white/10">
                      <div
                        className="h-2 rounded-full bg-gradient-to-r from-blue-500 to-cyan-400"
                        style={{ width: course.progress }}
                      />
                    </div>
                  </div>
                ))}
              </div>

              <div className="mt-6 grid grid-cols-2 gap-4">
                <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                  <p className="text-sm text-slate-400">Certificates</p>
                  <p className="mt-2 text-2xl font-bold text-white">12</p>
                </div>
                <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                  <p className="text-sm text-slate-400">Lessons done</p>
                  <p className="mt-2 text-2xl font-bold text-white">84</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function Features() {
  const items = [
    {
      icon: <BookOpen size={22} />,
      title: "Structured Courses",
      text: "Build and study full courses with lessons, files, and protected access.",
    },
    {
      icon: <ShieldCheck size={22} />,
      title: "Secure Access",
      text: "JWT authentication, role-based access, and enrollment checks for each course.",
    },
    {
      icon: <Users size={22} />,
      title: "Multi-role System",
      text: "Separate workflows for students, teachers, and administrators.",
    },
  ];

  return (
    <section id="features" className="px-6 py-20">
      <div className="mx-auto max-w-7xl">
        <div className="mb-12 max-w-2xl">
          <p className="mb-3 text-sm uppercase tracking-[0.2em] text-blue-300">Features</p>
          <h2 className="text-4xl font-bold text-white">Everything needed for a modern LMS</h2>
          <p className="mt-4 text-slate-300">
            Clean flows for authentication, course access, teacher approval, and learning progress.
          </p>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          {items.map((item) => (
            <div
              key={item.title}
              className="rounded-3xl border border-white/10 bg-white/5 p-6 transition hover:-translate-y-1 hover:bg-white/10"
            >
              <div className="mb-4 inline-flex rounded-2xl bg-blue-500/15 p-3 text-blue-300">
                {item.icon}
              </div>
              <h3 className="text-xl font-semibold text-white">{item.title}</h3>
              <p className="mt-3 leading-7 text-slate-300">{item.text}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function Roles() {
  const roles = [
    {
      title: "Student",
      text: "Browse courses, enroll, buy paid content, study lessons, and complete quizzes.",
    },
    {
      title: "Teacher",
      text: "Apply, get approved, create courses, upload lessons, and manage quizzes.",
    },
    {
      title: "Admin",
      text: "Review teacher applications, approve instructors, and control platform quality.",
    },
  ];

  return (
    <section id="roles" className="px-6 py-20">
      <div className="mx-auto max-w-7xl rounded-[32px] border border-white/10 bg-gradient-to-br from-white/5 to-white/[0.03] p-8 md:p-10">
        <div className="mb-10 max-w-2xl">
          <p className="mb-3 text-sm uppercase tracking-[0.2em] text-cyan-300">Roles</p>
          <h2 className="text-4xl font-bold text-white">Built for every user in the system</h2>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          {roles.map((role) => (
            <div key={role.title} className="rounded-3xl border border-white/10 bg-[#0d1b2a]/70 p-6">
              <h3 className="text-2xl font-bold text-white">{role.title}</h3>
              <p className="mt-4 leading-7 text-slate-300">{role.text}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function CoursesPreview() {
  const courses = [
    { title: "Spring Boot Basics", badge: "Popular", level: "Beginner" },
    { title: "React for LMS Frontend", badge: "New", level: "Intermediate" },
    { title: "MongoDB Data Modeling", badge: "Top Rated", level: "Intermediate" },
  ];

  return (
    <section id="courses" className="px-6 py-20">
      <div className="mx-auto max-w-7xl">
        <div className="mb-12 flex items-end justify-between gap-6">
          <div>
            <p className="mb-3 text-sm uppercase tracking-[0.2em] text-blue-300">Courses</p>
            <h2 className="text-4xl font-bold text-white">Discover premium learning paths</h2>
          </div>
          <a href="/student/courses" className="hidden text-slate-300 transition hover:text-white md:block">
            View all courses →
          </a>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          {courses.map((course) => (
            <div
              key={course.title}
              className="overflow-hidden rounded-[28px] border border-white/10 bg-[#10233a] transition hover:-translate-y-1 hover:shadow-2xl"
            >
              <div className="h-44 bg-gradient-to-br from-blue-500/30 via-cyan-400/20 to-transparent" />
              <div className="p-6">
                <div className="mb-4 flex items-center justify-between">
                  <span className="rounded-full bg-blue-400/10 px-3 py-1 text-xs font-semibold text-blue-200">
                    {course.badge}
                  </span>
                  <span className="text-sm text-slate-400">{course.level}</span>
                </div>

                <h3 className="text-2xl font-bold text-white">{course.title}</h3>
                <p className="mt-3 text-slate-300">
                  A clean modern course experience with videos, PDFs, quizzes, and progress tracking.
                </p>

                <a
                  href="/student/courses"
                  className="mt-6 inline-flex items-center gap-2 font-semibold text-blue-300 transition hover:text-blue-200"
                >
                  Open catalog
                  <ArrowRight size={16} />
                </a>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}

function Footer() {
  return (
    <footer className="border-t border-white/10 px-6 py-8">
      <div className="mx-auto flex max-w-7xl flex-col gap-4 text-sm text-slate-400 md:flex-row md:items-center md:justify-between">
        <div>© 2026 Diplom LMS</div>
        <div>React frontend for your LMS diploma project</div>
      </div>
    </footer>
  );
}

export default function HomePage() {
  return (
    <div className="min-h-screen bg-[#081120] text-white">
      <Header />
      <Hero />
      <Features />
      <Roles />
      <CoursesPreview />
      <Footer />
    </div>
  );
}