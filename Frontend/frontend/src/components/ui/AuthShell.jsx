export default function AuthShell({ title, subtitle, children, sideTitle, sideText }) {
  return (
    <div className="relative min-h-[calc(100vh-73px)] overflow-hidden bg-[#07111d]">
      <div className="absolute inset-0">
        <div className="absolute left-[-120px] top-[-80px] h-[320px] w-[320px] rounded-full bg-blue-500/20 blur-3xl" />
        <div className="absolute right-[-80px] top-[120px] h-[300px] w-[300px] rounded-full bg-cyan-400/10 blur-3xl" />
        <div className="absolute bottom-[-120px] left-[20%] h-[260px] w-[260px] rounded-full bg-sky-500/10 blur-3xl" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,rgba(255,255,255,0.05),transparent_35%)]" />
        <div className="absolute inset-0 opacity-[0.06] [background-image:linear-gradient(rgba(255,255,255,0.4)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.4)_1px,transparent_1px)] [background-size:36px_36px]" />
      </div>

      <div className="relative mx-auto grid min-h-[calc(100vh-73px)] max-w-7xl items-center gap-8 px-6 py-10 lg:grid-cols-[1.05fr_0.95fr]">
        <div className="hidden lg:block">
          <div className="max-w-xl">
            <div className="mb-4 inline-flex rounded-full border border-blue-400/20 bg-blue-400/10 px-4 py-2 text-sm font-medium text-blue-200">
              Deep Ocean Learning Experience
            </div>

            <h1 className="text-5xl font-bold leading-tight text-white">
              {sideTitle}
            </h1>

            <p className="mt-6 text-lg leading-8 text-slate-300">
              {sideText}
            </p>

            <div className="mt-10 grid gap-4 sm:grid-cols-2">
              <div className="rounded-3xl border border-white/10 bg-white/5 p-5 backdrop-blur">
                <div className="text-sm text-slate-400">Modern UI</div>
                <div className="mt-2 text-2xl font-bold text-white">Premium</div>
              </div>

              <div className="rounded-3xl border border-white/10 bg-white/5 p-5 backdrop-blur">
                <div className="text-sm text-slate-400">Learning flow</div>
                <div className="mt-2 text-2xl font-bold text-white">Smooth</div>
              </div>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-center">
          <div className="w-full max-w-md rounded-[32px] border border-white/10 bg-[#0d1b2a]/80 p-8 shadow-[0_20px_80px_rgba(0,0,0,0.45)] backdrop-blur-xl">
            <h2 className="text-3xl font-bold text-white">{title}</h2>
            <p className="mt-2 text-slate-400">{subtitle}</p>

            <div className="mt-8">{children}</div>
          </div>
        </div>
      </div>
    </div>
  );
}