// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/api/auth/login",
    REGISTER_CUSTOMER: "/api/auth/register-customer",
    REGISTER_OWNER: "/api/auth/register-owner",
    ME: "/api/auth/me",
    LOGOUT: "/api/auth/logout",
  },
};

// User Roles
export const ROLES = {
  SUPER_ADMIN: "SUPER_ADMIN",
  OWNER: "OWNER",
  CUSTOMER: "CUSTOMER",
};

// Route Names
export const ROUTE_NAMES = {
  HOME: "home",
  LOGIN: "login",
  REGISTER_CUSTOMER: "register-customer",
  REGISTER_OWNER: "register-owner",

  // Dashboard routes
  DASHBOARD: "dashboard",
  DASHBOARD_OWNER: "dashboard-owner",
  DASHBOARD_CUSTOMER: "dashboard-customer",
  DASHBOARD_ADMIN: "dashboard-admin",

  PROFILE: "profile",
  FORBIDDEN: "forbidden",
  NOT_FOUND: "not-found",
};

// Route Paths
export const ROUTE_PATHS = {
  HOME: "/",
  LOGIN: "/login",
  REGISTER_CUSTOMER: "/register/customer",
  REGISTER_OWNER: "/register/owner",

  DASHBOARD: "/dashboard",

  DASHBOARD_OWNER: "/dashboard/owner",
  DASHBOARD_CUSTOMER: "/dashboard/customer",
  DASHBOARD_ADMIN: "/dashboard/admin",

  PROFILE: "/profile",
  FORBIDDEN: "/403",
  NOT_FOUND: "/404",
};
