import api from "./api";
import { API_ENDPOINTS } from "../utils/constants";

export const authService = {
  async login(credentials) {
    const response = await api.post(API_ENDPOINTS.AUTH.LOGIN, credentials);
    return response.data;
  },

  async registerCustomer(userData) {
    const response = await api.post(
      API_ENDPOINTS.AUTH.REGISTER_CUSTOMER,
      userData,
    );
    return response.data;
  },

  async registerOwner(userData) {
    const response = await api.post(
      API_ENDPOINTS.AUTH.REGISTER_OWNER,
      userData,
    );
    return response.data;
  },

  async getCurrentUser() {
    const response = await api.get(API_ENDPOINTS.AUTH.ME);
    return response.data;
  },

  async logout() {
    const response = await api.post(API_ENDPOINTS.AUTH.LOGOUT);
    return response.data;
  },
};

export default authService;
