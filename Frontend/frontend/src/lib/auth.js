function normalizeRole(role) {
  if (!role) return "";
  return String(role).replace("ROLE_", "").trim().toUpperCase();
}

export function saveAuth(data) {
  if (!data) return;

  if (data.token) {
    localStorage.setItem("token", data.token);
  }

  if (data.role) {
    localStorage.setItem("role", normalizeRole(data.role));
  }

  if (data.email) {
    localStorage.setItem("email", data.email);
  }

  if (typeof data.teacherApproved !== "undefined") {
    localStorage.setItem("teacherApproved", String(data.teacherApproved));
  }
}

export function clearAuth() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("email");
  localStorage.removeItem("teacherApproved");
}

export function getToken() {
  return localStorage.getItem("token");
}

export function getRole() {
  return localStorage.getItem("role");
}

export function isTeacherApproved() {
  return localStorage.getItem("teacherApproved") === "true";
}

export function isAuthenticated() {
  return Boolean(getToken());
}