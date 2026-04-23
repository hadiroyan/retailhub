import { createRouter, createWebHistory } from "vue-router";
import { ROUTE_PATHS, ROUTE_NAMES } from "../utils/constants";
import { useAuthStore } from "../stores/auth";

// Existing views
import Home from "../views/Home.vue";
import Login from "../views/auth/Login.vue";
import RegisterChoice from "../views/auth/RegisterChoice.vue";
import RegisterCustomer from "../views/auth/RegisterCustomer.vue";
import RegisterOwner from "../views/auth/RegisterOwner.vue";
import OwnerDashboard from "../views/dashboard/OwnerDashboard.vue";
import CustomerDashboard from "../views/dashboard/CustomerDashboard.vue";
import SuperAdminDashboard from "../views/dashboard/SuperAdminDashboard.vue";
import Profile from "../views/profile/Profile.vue";
import NotFound from "../views/NotFound.vue";
import Forbidden from "../views/Forbidden.vue";

// Store views
import CreateStorePage from "../views/stores/CreateStorePage.vue";
import EditStorePage from "../views/stores/EditStorePage.vue";
import EmployeeListPage from "../views/stores/EmployeeListPage.vue";
import CategoryListPage from "../views/stores/CategoryListPage.vue";
import ProductListPage from "../views/stores/ProductListPage.vue";
import StoreSettingPage from "../views/stores/StoreSettingPage.vue";
import StoreDashboardPage from "../views/stores/StoreDashboardPage.vue";

const routes = [
  // =========================================================================
  // Public Routes
  // =========================================================================
  {
    path: ROUTE_PATHS.HOME,
    name: ROUTE_NAMES.HOME,
    component: Home,
  },

  // =========================================================================
  // Auth Routes
  // =========================================================================
  {
    path: ROUTE_PATHS.LOGIN,
    name: ROUTE_NAMES.LOGIN,
    component: Login,
    meta: { guest: true },
  },
  {
    path: "/register",
    name: "register",
    component: RegisterChoice,
    meta: { guest: true },
  },
  {
    path: ROUTE_PATHS.REGISTER_CUSTOMER,
    name: ROUTE_NAMES.REGISTER_CUSTOMER,
    component: RegisterCustomer,
    meta: { guest: true },
  },
  {
    path: ROUTE_PATHS.REGISTER_OWNER,
    name: ROUTE_NAMES.REGISTER_OWNER,
    component: RegisterOwner,
    meta: { guest: true },
  },

  // =========================================================================
  // Dashboard Routes
  // =========================================================================
  {
    path: ROUTE_PATHS.DASHBOARD,
    name: ROUTE_NAMES.DASHBOARD,
    redirect: () => {
      const authStore = useAuthStore();
      const role = authStore.userRole;
      if (role === "OWNER") return { name: ROUTE_NAMES.DASHBOARD_OWNER };
      if (role === "CUSTOMER") return { name: ROUTE_NAMES.DASHBOARD_CUSTOMER };
      if (role === "SUPER_ADMIN") return { name: ROUTE_NAMES.DASHBOARD_ADMIN };
      if (role === "ADMIN" || role === "MANAGER" || role === "STAFF") {
        const assignedStoreId = authStore.assignedStore?.id;
        if (assignedStoreId) {
          return {
            name: ROUTE_NAMES.EMPLOYEE_STORE_DASHBOARD,
            params: { storeId: assignedStoreId }
          };
        }
        return { name: ROUTE_NAMES.LOGIN };
      }
    },
    meta: { requiresAuth: true },
  },
  {
    path: ROUTE_PATHS.DASHBOARD_OWNER,
    name: ROUTE_NAMES.DASHBOARD_OWNER,
    component: OwnerDashboard,
    meta: {
      requiresAuth: true,
      roles: ["OWNER"],
      title: "My Stores",
    },
  },
  {
    path: ROUTE_PATHS.DASHBOARD_CUSTOMER,
    name: ROUTE_NAMES.DASHBOARD_CUSTOMER,
    component: CustomerDashboard,
    meta: {
      requiresAuth: true,
      roles: ["CUSTOMER"],
      title: "Customer Dashboard",
    },
  },
  {
    path: ROUTE_PATHS.DASHBOARD_ADMIN,
    name: ROUTE_NAMES.DASHBOARD_ADMIN,
    component: SuperAdminDashboard,
    meta: {
      requiresAuth: true,
      roles: ["SUPER_ADMIN"],
      title: "Admin Dashboard",
    },
  },

  // =========================================================================
  // Owner Store Management
  // =========================================================================
  {
    path: ROUTE_PATHS.OWNER_STORES,
    name: ROUTE_NAMES.OWNER_STORES,
    component: OwnerDashboard,
    meta: {
      requiresAuth: true,
      roles: ["OWNER"],
      title: "My Stores",
    },
  },
  {
    path: ROUTE_PATHS.OWNER_STORE_CREATE,
    name: ROUTE_NAMES.OWNER_STORE_CREATE,
    component: CreateStorePage,
    meta: {
      requiresAuth: true,
      roles: ["OWNER"],
      title: "Create Store",
    },
  },
  {
    path: ROUTE_PATHS.OWNER_STORE_EDIT,
    name: ROUTE_NAMES.OWNER_STORE_EDIT,
    component: EditStorePage,
    meta: {
      requiresAuth: true,
      roles: ["OWNER"],
      title: "Edit Store",
    },
  },

  // =========================================================================
  // Store Context Routes (placeholder — to be filled in on the following pages)
  // =========================================================================
  {
    path: ROUTE_PATHS.STORE_DASHBOARD,
    name: ROUTE_NAMES.STORE_DASHBOARD,
    component: StoreDashboardPage,
    meta: {
      requiresAuth: true,
      roles: ["OWNER", "ADMIN", "MANAGER", "STAFF"],
      title: "Store Dashboard",
    },
  },
  {
    path: ROUTE_PATHS.STORE_EMPLOYEES,
    name: ROUTE_NAMES.STORE_EMPLOYEES,
    component: EmployeeListPage,
    meta: {
      requiresAuth: true,
      roles: ["OWNER", "ADMIN", "MANAGER"],
      title: "Employees",
    },
  },
  {
    path: ROUTE_PATHS.STORE_CATEGORIES,
    name: ROUTE_NAMES.STORE_CATEGORIES,
    component: CategoryListPage,
    meta: {
      requiresAuth: true,
      roles: ["OWNER", "ADMIN", "MANAGER", "STAFF"],
      title: "Categories",
    },
  },
  {
    path: ROUTE_PATHS.STORE_PRODUCTS,
    name: ROUTE_NAMES.STORE_PRODUCTS,
    component: ProductListPage,
    meta: {
      requiresAuth: true,
      roles: ["OWNER", "ADMIN", "MANAGER", "STAFF"],
      title: "Products",
    },
  },
  {
    path: ROUTE_PATHS.STORE_SETTINGS,
    name: ROUTE_NAMES.STORE_SETTINGS,
    component: StoreSettingPage,
    meta: {
      requiresAuth: true,
      roles: ["OWNER"],
      title: "Store Settings",
    },
  },

  // =========================================================================
  // Employee
  // =========================================================================
  {
    path: ROUTE_PATHS.EMPLOYEE_STORE_DASHBOARD,
    name: ROUTE_NAMES.EMPLOYEE_STORE_DASHBOARD,
    component: StoreDashboardPage,
    meta: {
      requiresAuth: true,
      roles: ["ADMIN", "MANAGER", "STAFF"],
      title: "Store Dashboard",
    },
  },
  {
    path: ROUTE_PATHS.EMPLOYEE_STORE_EMPLOYEES,
    name: ROUTE_NAMES.EMPLOYEE_STORE_EMPLOYEES,
    component: () => import("../views/stores/EmployeeListPage.vue"),
    meta: {
      requiresAuth: true,
      roles: ["ADMIN", "MANAGER"],
      title: "Employees",
    },
  },
  {
    path: ROUTE_PATHS.EMPLOYEE_STORE_CATEGORIES,
    name: ROUTE_NAMES.EMPLOYEE_STORE_CATEGORIES,
    component: CategoryListPage,
    meta: {
      requiresAuth: true,
      roles: ["ADMIN", "MANAGER", "STAFF"],
      title: "Categories",
    },
  },
  {
    path: ROUTE_PATHS.EMPLOYEE_STORE_PRODUCTS,
    name: ROUTE_NAMES.EMPLOYEE_STORE_PRODUCTS,
    component: ProductListPage,
    meta: {
      requiresAuth: true,
      roles: ["ADMIN", "MANAGER", "STAFF"],
      title: "Products",
    },
  },

  // =========================================================================
  // Profile
  // =========================================================================
  {
    path: ROUTE_PATHS.PROFILE,
    name: ROUTE_NAMES.PROFILE,
    component: Profile,
    meta: {
      requiresAuth: true,
      title: "Profile",
    },
  },

  // =========================================================================
  // Error Pages
  // =========================================================================
  {
    path: ROUTE_PATHS.FORBIDDEN,
    name: ROUTE_NAMES.FORBIDDEN,
    component: Forbidden,
    meta: { title: "403 - Forbidden" },
  },
  {
    path: ROUTE_PATHS.NOT_FOUND,
    name: ROUTE_NAMES.NOT_FOUND,
    component: NotFound,
    meta: { title: "404 - Not Found" },
  },

  // Catch-all (must be last)
  {
    path: "/:pathMatch(.*)*",
    redirect: { name: ROUTE_NAMES.NOT_FOUND },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();

  // Set page title
  document.title = to.meta.title
    ? `${to.meta.title} - RetailHub`
    : "RetailHub - Multi-Store Retail Management";

  const loggedIn = await authStore.checkAuth();

  // ================= PROTECTED ROUTE =================
  if (to.meta.requiresAuth && !loggedIn) {
    return {
      name: ROUTE_NAMES.LOGIN,
      query: { redirect: to.fullPath },
    };
  }

  // ================= ROLE CHECK =================
  if (to.meta.roles && !to.meta.roles.includes(authStore.userRole)) {
    return { name: ROUTE_NAMES.FORBIDDEN };
  }

  // ================= GUEST ONLY =================
  if (to.meta.guest && loggedIn) {
    return { name: ROUTE_NAMES.DASHBOARD };
  }

  return true;
});

router.afterEach((to, from) => {
  window.scrollTo(0, 0);
  console.log(`Navigated from ${from.path} to ${to.path}`);
});

export default router;
