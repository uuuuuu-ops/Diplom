import { Navigate, useLocation } from "react-router-dom";
import { getRole, isAuthenticated } from "../../lib/auth";

function normalizeRole(role) {
  if (!role) return "";
  return String(role).replace("ROLE_", "").trim().toUpperCase();
}

export default function ProtectedRoute({ children, allowedRoles = [] }) {
  const location = useLocation();
  const authenticated = isAuthenticated();
  const role = normalizeRole(getRole());

  if (!authenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}