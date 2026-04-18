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

  STORES: {
    BASE: "/api/v1/stores",
    BY_ID: (id) => `/api/v1/stores/${id}`,
    BY_SLUG: (slug) => `/api/v1/stores/${slug}`,
    STATUS: (id) => `/api/v1/stores/${id}/status`,
  },
 
  EMPLOYEES: {
    BASE: (storeId) => `/api/v1/stores/${storeId}/employees`,
    BY_ID: (storeId, userId) => `/api/v1/stores/${storeId}/employees/${userId}`,
  },
 
  CATEGORIES: {
    BASE: (storeId) => `/api/v1/stores/${storeId}/categories`,
    BY_ID: (storeId, id) => `/api/v1/stores/${storeId}/categories/${id}`,
    BY_SLUG: (storeId, slug) => `/api/v1/stores/${storeId}/categories/${slug}`,
  },
 
  PRODUCTS: {
    BASE: (storeId) => `/api/v1/stores/${storeId}/products`,
    BY_ID: (storeId, id) => `/api/v1/stores/${storeId}/products/${id}`,
    BY_SKU: (storeId, sku) => `/api/v1/stores/${storeId}/products/${sku}`,
    DETAIL: (storeId, sku) => `/api/v1/stores/${storeId}/products/${sku}/detail`,
  },
};

// User Roles
export const ROLES = {
  SUPER_ADMIN: "SUPER_ADMIN",
  OWNER: "OWNER",
  CUSTOMER: "CUSTOMER",
  MANAGER: "MANAGER",
  STAFF: "STAFF",
  CUSTOMER: "CUSTOMER",
};

// Store Status
export const STORE_STATUS = {
  ACTIVE: "ACTIVE",
  CLOSED: "CLOSED",
  SUSPEND: "SUSPEND",
};
 
// Product Status
export const PRODUCT_STATUS = {
  ACTIVE: "ACTIVE",
  DRAFT: "DRAFT",
  OUT_OF_STOCK: "OUT_OF_STOCK",
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
 
  // Owner store management
  OWNER_STORES: "owner-stores",
  OWNER_STORE_CREATE: "owner-store-create",
  OWNER_STORE_EDIT: "owner-store-edit",
 
  // Store context (OWNER inside a store, ADMIN/MANAGER/STAFF)
  STORE_DASHBOARD: "store-dashboard",
  STORE_SETTINGS: "store-settings",
  STORE_EMPLOYEES: "store-employees",
  STORE_CATEGORIES: "store-categories",
  STORE_PRODUCTS: "store-products",
 
  // Public
  EXPLORE_STORES: "explore-stores",
  STORE_DETAIL: "store-detail",
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

  // Owner store management
  OWNER_STORES: "/dashboard/owner/stores",
  OWNER_STORE_CREATE: "/dashboard/owner/stores/create",
  OWNER_STORE_EDIT: "/dashboard/owner/stores/:id/edit",
 
  // Store context — OWNER masuk store tertentu
  STORE_DASHBOARD: "/dashboard/owner/stores/:storeId",
  STORE_SETTINGS: "/dashboard/owner/stores/:storeId/settings",
  STORE_EMPLOYEES: "/dashboard/owner/stores/:storeId/employees",
  STORE_CATEGORIES: "/dashboard/owner/stores/:storeId/categories",
  STORE_PRODUCTS: "/dashboard/owner/stores/:storeId/products",
 
  // Public
  EXPLORE_STORES: "/stores",
  STORE_DETAIL: "/stores/:slug",
};
