import { Outlet, Link, useNavigate } from "react-router-dom";
import { clearAuth, getRole, isAuthenticated } from "../lib/auth";

export default function MainLayout() {
  const navigate = useNavigate();

  const authenticated = isAuthenticated();
  const role = getRole();

  function handleLogout() {
    clearAuth();
    navigate("/login");
  }

  return (
    <div className="min-h-screen bg-[#081120] text-white">
      <header className="border-b border-white/10 bg-[#0b1726]/80 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <Link to="/" className="text-xl font-bold tracking-wide">
            Diplom LMS
          </Link>

          <nav className="flex items-center gap-6 text-sm text-slate-300">
            <Link to="/student/courses" className="hover:text-white">
              Courses
            </Link>

            {!authenticated && (
              <>
                <Link to="/login" className="hover:text-white">
                  Login
                </Link>
                <Link to="/register" className="hover:text-white">
                  Register
                </Link>
              </>
            )}

            {authenticated && (
              <>
                <span className="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs text-slate-300">
                  {role || "USER"}
                </span>
                <button onClick={handleLogout} className="hover:text-white">
                  Logout
                </button>
              </>
            )}
          </nav>
        </div>
      </header>

      <main>
        <Outlet />
      </main>
    </div>
  );
}