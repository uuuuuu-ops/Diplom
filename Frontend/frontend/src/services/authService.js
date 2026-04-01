import api from "./api";

export async function loginUser(payload) {
  const response = await api.post("/auth/login", payload);
  return response.data;
  
}

export async function registerUser(payload) {
  const response = await api.post("/auth/register", payload);
  return response.data;
}

export async function verifyEmail(payload) {
  const response = await api.post("/auth/verify", payload);
  return response.data;
}