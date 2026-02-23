import { defineStore } from "pinia";
import { ref, computed } from "vue";
import authService from "../services/authService";

export const useAuthStore = defineStore("auth", () => {
  // State
  const user = ref(null);
  const loading = ref(false);
  const error = ref(null);

  const authChecked = ref(false);
  const authCheckPromise = ref(null);

  //   Getters
  const isAuthenticated = computed(() => !!user.value);
  const userRole = computed(() => user.value?.roles[0] || null);
  const userName = computed(() => user.value?.fullName || "");
  const userEmail = computed(() => user.value?.email || "");

  async function checkAuth() {
    if (authCheckPromise.value) {
      return authCheckPromise.value;
    }

    if (authChecked.value) {
      return isAuthenticated.value;
    }

    authCheckPromise.value = fetchCurrentUser()
      .then(() => {
        authChecked.value = true;
        return true;
      })
      .catch(() => {
        authChecked.value = true;
        return false;
      })
      .finally(() => {
        authCheckPromise.value = null;
      });

    return authCheckPromise.value;
  }

  async function login(credentials) {
    loading.value = true;
    error.value = null;

    try {
      const response = await authService.login(credentials);
      user.value = response.data.user || response;

      authChecked.value = true;
      console.log("Login successfull:", user.value);
      return response;
    } catch (err) {
      const errorMessage =
        err.response?.data?.message ||
        "Login failed. Please check your credentials";
      error.value = errorMessage;
      console.error("Login error:", errorMessage);
    } finally {
      loading.value = false;
    }
  }

  async function registerCustomer(userData) {
    loading.value = true;
    error.value = null;

    try {
      const response = await authService.registerCustomer(userData);
      user.value = response.data?.user || response.data;
      authChecked.value = true;
      console.log("Customer registration successful");
      return response;
    } catch (err) {
      const errorMessage =
        err.response?.data?.message || "Registration failed. Please try again.";
      error.value = errorMessage;
      console.error("Registration error:", errorMessage);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function registerOwner(userData) {
    loading.value = true;
    error.value = null;

    try {
      const response = await authService.registerOwner(userData);
      user.value = response.data?.user || response.data;
      authChecked.value = true;
      console.log("Owner registration successful");
      return response;
    } catch (err) {
      const errorMessage =
        err.response?.data?.message || "Registration failed. Please try again.";
      error.value = errorMessage;
      console.error("Registration error:", errorMessage);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchCurrentUser() {
    error.value = null;

    try {
      const response = await authService.getCurrentUser();
      user.value = response.data || response.user || response;

      console.log("Session restored. Current user:", userName.value);
      return response;
    } catch (err) {
      // If 401, user not authenticated - this is expected
      if (err.response?.status === 401) {
        user.value = null;
        console.log("No active session (not logged in)");
      } else {
        const errorMessage =
          err.response?.data?.message || "Failed to fetch user data.";
        error.value = errorMessage;
        console.error("Fetch user error:", errorMessage);
      }
      throw err;
    }
  }

  async function logout() {
    loading.value = true;
    error.value = null;

    try {
      await authService.logout();
      console.log("Logout successful");
    } catch (err) {
      console.error("Logout error (cleared local state anyway):", err);
    } finally {
      resetAuth();
      loading.value = false;
    }
  }

  function clearError() {
    error.value = null;
  }

  function resetAuth() {
    user.value = null;
    loading.value = false;
    error.value = null;

    authChecked.value = false;
    authCheckPromise.value = null;

    console.log("Auth state reset");
  }

  return {
    // State
    user,
    loading,
    error,

    // Getters
    isAuthenticated,
    userRole,
    userName,
    userEmail,

    // Actions
    checkAuth,
    login,
    registerCustomer,
    registerOwner,
    fetchCurrentUser,
    logout,
    clearError,
    resetAuth,
  };
});
