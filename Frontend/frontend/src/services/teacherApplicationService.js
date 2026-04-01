import api from "./api";

export async function getMyTeacherApplication() {
  const response = await api.get("/teacher-applications/my");
  return response.data;
}