import { createRouter, createWebHistory } from "vue-router";
import { ROUTE_PATHS, ROUTE_NAMES } from "../utils/constants";
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
import { useAuthStore } from "../stores/auth";

const routes = [
  // Public Routes
  {
    path: ROUTE_PATHS.HOME,
    name: ROUTE_NAMES.HOME,
    component: Home,
  },

  // Authentication Routes
  {
    path: ROUTE_PATHS.LOGIN,
    name: ROUTE_NAMES.LOGIN,
    component: Login,
    meta: { guest: true }, // Only accessible when NOT authenticated
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

  // Dashboard Routes
  {
    path: ROUTE_PATHS.DASHBOARD,
    name: ROUTE_NAMES.DASHBOARD,
    redirect: () => {
      const authStore = useAuthStore();
      const role = authStore.userRole;

      // Redirect based on user role
      if (role === "OWNER") return { name: ROUTE_NAMES.DASHBOARD_OWNER };
      if (role === "CUSTOMER") return { name: ROUTE_NAMES.DASHBOARD_CUSTOMER };
      if (role === "SUPER_ADMIN") return { name: ROUTE_NAMES.DASHBOARD_ADMIN };

      // If no role or not authenticated, go to login
      return { name: ROUTE_NAMES.LOGIN };
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
      title: "Owner Dashboard",
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

  // Profile Route
  {
    path: ROUTE_PATHS.PROFILE,
    name: ROUTE_NAMES.PROFILE,
    component: Profile,
    meta: {
      requiresAuth: true,
      title: "Profile",
    },
  },

  // Error Pages
  {
    path: ROUTE_PATHS.FORBIDDEN,
    name: ROUTE_NAMES.FORBIDDEN,
    component: Forbidden,
    meta: {
      title: "403 - Forbidden",
    },
  },
  {
    path: ROUTE_PATHS.NOT_FOUND,
    name: ROUTE_NAMES.NOT_FOUND,
    component: NotFound,
    meta: {
      title: "404 - Not Found",
    },
  },

  // Catch-all route (must be last)
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
