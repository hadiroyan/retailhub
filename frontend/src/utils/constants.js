// API Configuration
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/api/auth/login",
    REGISTER_CUSTOMER: "/api/auth/register-customer",
    REGISTER_OWNER: "/api/auth/register-owner",
    ME: "/api/auth/me",
    LOGOUT: "/api/auth/logout",
    UPDATE_PROFILE: "/api/auth/me",
    CHANGE_PASSWORD: "/api/auth/change-password",
    VERIFY_EMAIL: '/api/auth/verify-email',
    RESEND_OTP: '/api/auth/resend-otp',
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
    INTERNAL: (storeId) => `/api/v1/stores/${storeId}/products/internal`,
    BY_ID: (storeId, id) => `/api/v1/stores/${storeId}/products/${id}`,
    BY_SKU: (storeId, sku) => `/api/v1/stores/${storeId}/products/${sku}`,
    DETAIL: (storeId, sku) => `/api/v1/stores/${storeId}/products/${sku}/detail`,
    IMAGES: (storeId, productId) => `/api/v1/stores/${storeId}/products/${productId}/images`,
    IMAGE_DELETE: (storeId, productId) => `/api/v1/stores/${storeId}/products/${productId}/images`,
  },

  EXPLORE: {
    PRODUCTS: "/api/v1/products",
    STORE_PRODUCTS: (slug) => `/api/v1/stores/${slug}/products`,
  },

  ORDERS: {
    BASE: "/api/v1/orders",
    BY_ID: (id) => `/api/v1/orders/${id}`,
    STORE_ORDERS: (storeId) => `/api/v1/stores/${storeId}/orders`,
    STORE_ORDER_BY_ID: (storeId, id) => `/api/v1/stores/${storeId}/orders/${id}`,
    STORE_ORDER_STATUS: (storeId, id) => `/api/v1/stores/${storeId}/orders/${id}/status`,
  },

  SUPPLIERS: {
    BASE: (storeId) => `/api/v1/stores/${storeId}/suppliers`,
    BY_ID: (storeId, id) => `/api/v1/stores/${storeId}/suppliers/${id}`,
  },
  PURCHASE_ORDERS: {
    BASE: (storeId) => `/api/v1/stores/${storeId}/purchase-orders`,
    BY_ID: (storeId, id) => `/api/v1/stores/${storeId}/purchase-orders/${id}`,
    STATUS: (storeId, id) => `/api/v1/stores/${storeId}/purchase-orders/${id}/status`,
  },
};

// User Roles
export const ROLES = {
  SUPER_ADMIN: "SUPER_ADMIN",
  OWNER: "OWNER",
  CUSTOMER: "CUSTOMER",
  ADMIN: "ADMIN",
  MANAGER: "MANAGER",
  STAFF: "STAFF",
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

  EMPLOYEE_STORE_DASHBOARD: "employee-store-dashboard",
  EMPLOYEE_STORE_EMPLOYEES: "employee-store-employees",
  EMPLOYEE_STORE_CATEGORIES: "employee-store-categories",
  EMPLOYEE_STORE_PRODUCTS: "employee-store-products",
  EMPLOYEE_STORE_ORDERS: "employee-store-orders",
  EMPLOYEE_STORE_SUPPLIER: "employee-store-supplier",
  EMPLOYEE_STORE_PURCHASE_ORDER: "employee-store-purchase-order",

  // Store context (OWNER inside a store, ADMIN/MANAGER/STAFF)
  STORE_DASHBOARD: "store-dashboard",
  STORE_SETTINGS: "store-settings",
  STORE_EMPLOYEES: "store-employees",
  STORE_CATEGORIES: "store-categories",
  STORE_PRODUCTS: "store-products",
  STORE_ORDERS: "store-orders",
  STORE_SUPPLIER: "store-supplier",
  STORE_PURCHASE_ORDER: "store-purchase-order",

  // Public
  EXPLORE_STORES: "explore-stores",
  STORE_DETAIL: "store-detail",

  EXPLORE: "explore",
  STORE_DETAIL: "store-detail",
  PRODUCT_DETAIL: "product-detail",
  CART: "cart",
  CHECKOUT: "checkout",
  ORDER_HISTORY: "order-history",
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

  // Tambah path baru
  EMPLOYEE_STORE_DASHBOARD: "/dashboard/store/:storeId",
  EMPLOYEE_STORE_EMPLOYEES: "/dashboard/store/:storeId/employees",
  EMPLOYEE_STORE_CATEGORIES: "/dashboard/store/:storeId/categories",
  EMPLOYEE_STORE_PRODUCTS: "/dashboard/store/:storeId/products",
  EMPLOYEE_STORE_ORDERS: "/dashboard/store/:storeId/orders",
  EMPLOYEE_STORE_SUPPLIER: "/dashboard/store/:storeId/supplier",
  EMPLOYEE_STORE_PURCHASE_ORDER: "/dashboard/store/:storeId/purchase-order",

  // Store context — OWNER masuk store tertentu
  STORE_DASHBOARD: "/dashboard/owner/stores/:storeId",
  STORE_SETTINGS: "/dashboard/owner/stores/:storeId/settings",
  STORE_EMPLOYEES: "/dashboard/owner/stores/:storeId/employees",
  STORE_CATEGORIES: "/dashboard/owner/stores/:storeId/categories",
  STORE_PRODUCTS: "/dashboard/owner/stores/:storeId/products",
  STORE_ORDERS: "/dashboard/owner/stores/:storeId/orders",
  STORE_SUPPLIER: "/dashboard/owner/stores/:storeId/supplier",
  STORE_PURCHASE_ORDER: "/dashboard/owner/stores/:storeId/purchase-order",

  // Public
  EXPLORE_STORES: "/stores",
  STORE_DETAIL: "/stores/:slug",

  EXPLORE: "/explore",
  STORE_DETAIL: "/stores/:slug",
  PRODUCT_DETAIL: "/stores/:storeSlug/products/:sku",
  CART: "/cart",
  CHECKOUT: "/checkout",
  ORDER_HISTORY: "/orders",
};
