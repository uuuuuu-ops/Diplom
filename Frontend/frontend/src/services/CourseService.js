import api from "./api";

export async function getPublicCourses() {
  const response = await api.get("/courses/public");
  return response.data;
}

export async function getCourseById(courseId) {
  const response = await api.get(`/courses/${courseId}`);
  return response.data;
}

export async function getMyCourses() {
  const response = await api.get("/courses/my");
  return response.data;
}

export async function createCourse(formData) {
  const response = await api.post("/courses", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
}

export async function updateCourse(courseId, formData) {
  const response = await api.put(`/courses/${courseId}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
}

export async function deleteCourse(courseId) {
  const response = await api.delete(`/courses/${courseId}`);
  return response.data;
}

export async function createLesson(courseId, formData) {
  const response = await api.post(`/lessons/course/${courseId}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
}

export async function getLessonsByCourseId(courseId) {
  const response = await api.get(`/lessons/course/${courseId}`);
  return response.data;
}

export async function getLessonById(lessonId) {
  const response = await api.get(`/lessons/${lessonId}`);
  return response.data;
}

export async function updateLesson(lessonId, formData) {
  const response = await api.put(`/lessons/${lessonId}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
}

export async function deleteLesson(lessonId) {
  const response = await api.delete(`/lessons/${lessonId}`);
  return response.data;
}