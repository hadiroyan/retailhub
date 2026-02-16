import axios from "axios";
import { API_BASE_URL } from "../utils/constants";

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    console.log(`Request:`, config.method?.toUpperCase(), config.url);
    return config;
  },
  (error) => {
    console.error("Request error: ", error);
    return Promise.reject(error);
  },
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    console.log("✅ Response:", response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error("Response Error:", error.response?.status, error.config?.url);

    // Handle common errors
    if (error.response?.status === 401) {
      // Unauthorized - redirect to login
      // TODO: handle with auth store
      console.log("🔒 Unauthorized - Need to login");
    }

    if (error.response?.status === 403) {
      // Forbidden - no permission
      console.log("🚫 Forbidden - No permission");
    }

    return Promise.reject(error);
  },
);

export default api;
