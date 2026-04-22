<template>
  <div class="min-h-screen flex flex-col bg-gray-50">

    <!-- Topbar -->
    <header class="bg-white border-b border-gray-200 h-16 flex items-center px-4 sticky top-0 z-50 shadow-sm">
      <div class="flex items-center justify-between w-full">

        <!-- Left: toggle + brand -->
        <div class="flex items-center gap-3">
          <button @click="toggleSidebar"
            class="p-2 rounded-lg text-gray-500 hover:bg-gray-100 hover:text-blue-600 transition-colors">
            <i :class="['fas', sidebarOpen ? 'fa-times' : 'fa-bars']"></i>
          </button>
          <span class="text-xl font-bold text-blue-600 cursor-pointer"
            @click="router.push({ name: ROUTE_NAMES.DASHBOARD })">
            RetailHub
          </span>
          <!-- Store context badge -->
          <span v-if="activeStore" class="hidden sm:flex items-center gap-1 text-sm text-gray-500">
            <i class="fas fa-chevron-right text-xs text-gray-300"></i>
            <span class="font-medium text-gray-700">{{ activeStore.name }}</span>
          </span>
        </div>

        <!-- Right: user info + logout -->
        <div class="flex items-center gap-3">
          <div class="hidden sm:flex flex-col items-end">
            <span class="text-sm font-medium text-gray-800">{{ userName }}</span>
            <span class="text-xs px-2 py-0.5 rounded-full font-medium" :class="roleBadgeClass">
              {{ userRole }}
            </span>
          </div>
          <button @click="handleLogout"
            class="flex items-center gap-1 px-3 py-2 text-sm text-white bg-red-500 hover:bg-red-600 rounded-lg transition-colors cursor-pointer">
            <i class="fas fa-sign-out-alt"></i>
            <span class="hidden sm:inline">Logout</span>
          </button>
        </div>

      </div>
    </header>

    <!-- Body -->
    <div class="flex flex-1 overflow-hidden">

      <!-- Sidebar -->
      <aside :class="[
        'bg-white border-r border-gray-200 flex flex-col transition-all duration-300 ease-in-out overflow-hidden shrink-0',
        sidebarOpen ? 'w-56' : 'w-16'
      ]">
        <nav class="flex-1 py-4 flex flex-col gap-1 px-2 overflow-y-auto">

          <!-- Back to My Stores — only when inside a store context (OWNER) -->
          <template v-if="activeStore && isOwner">
            <button @click="goBackToMyStores"
              class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-gray-500 hover:bg-gray-100 hover:text-blue-600 transition-colors w-full cursor-pointer">
              <i class="fas fa-arrow-left w-4 text-center shrink-0"></i>
              <span v-if="sidebarOpen" class="truncate whitespace-nowrap">My Stores</span>
            </button>
            <div class="border-t border-gray-100 my-1"></div>
          </template>

          <!-- Nav items -->
          <button v-for="item in navItems" :key="item.name" @click="navigate(item)"
            :title="!sidebarOpen ? item.name : ''" :class="[
              'flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors w-full cursor-pointer',
              isActive(item)
                ? 'bg-blue-50 text-blue-600 font-medium border-r-2 border-blue-600'
                : 'text-gray-600 hover:bg-gray-50 hover:text-blue-600'
            ]">
            <i :class="`fas ${item.icon} w-4 text-center shrink-0`"></i>
            <span v-if="sidebarOpen" class="truncate whitespace-nowrap">{{ item.name }}</span>
          </button>

        </nav>
      </aside>

      <!-- Main content -->
      <main class="flex-1 overflow-y-auto p-6">
        <slot></slot>
      </main>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '../stores/auth';
import { useStoreStore } from '../stores/storeStore';
import { ROUTE_NAMES, ROLES } from '../utils/constants';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const storeStore = useStoreStore();

const { userName, userRole } = storeToRefs(authStore);
const { activeStore } = storeToRefs(storeStore);

const sidebarOpen = ref(true);

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value;
};

// Role checks
const isOwner = computed(() => userRole.value === ROLES.OWNER);
const isAdmin = computed(() => userRole.value === ROLES.ADMIN);
const isManager = computed(() => userRole.value === ROLES.MANAGER);
const isStaff = computed(() => userRole.value === ROLES.STAFF);
const isSuperAdmin = computed(() => userRole.value === ROLES.SUPER_ADMIN);

// Role badge color
const roleBadgeClass = computed(() => {
  const map = {
    OWNER: 'bg-blue-100 text-blue-700',
    ADMIN: 'bg-sky-100 text-sky-700',
    MANAGER: 'bg-green-100 text-green-700',
    STAFF: 'bg-emerald-100 text-emerald-700',
    SUPER_ADMIN: 'bg-indigo-100 text-indigo-700',
  };
  return map[userRole.value] || 'bg-gray-100 text-gray-700';
});

// Nav items per role
const navItems = computed(() => {
  const storeId = activeStore.value?.id;

  // OWNER — inside store context
  if (isOwner.value && storeId) {
    return [
      { name: 'Dashboard', icon: 'fa-chart-bar', routeName: ROUTE_NAMES.STORE_DASHBOARD, params: { storeId } },
      { name: 'Employees', icon: 'fa-users', routeName: ROUTE_NAMES.STORE_EMPLOYEES, params: { storeId } },
      { name: 'Categories', icon: 'fa-folder', routeName: ROUTE_NAMES.STORE_CATEGORIES, params: { storeId } },
      { name: 'Products', icon: 'fa-box', routeName: ROUTE_NAMES.STORE_PRODUCTS, params: { storeId } },
      { name: 'Settings', icon: 'fa-cog', routeName: ROUTE_NAMES.STORE_SETTINGS, params: { storeId } },
    ];
  }

  // OWNER — no store selected yet
  if (isOwner.value) {
    return [
      { name: 'My Stores', icon: 'fa-store', routeName: ROUTE_NAMES.OWNER_STORES },
      { name: 'Profile', icon: 'fa-user', routeName: ROUTE_NAMES.PROFILE },
    ];
  }

  // ADMIN
  if (isAdmin.value) {
    return [
      { name: 'Dashboard', icon: 'fa-chart-bar', routeName: ROUTE_NAMES.STORE_DASHBOARD },
      { name: 'Employees', icon: 'fa-users', routeName: ROUTE_NAMES.STORE_EMPLOYEES },
      { name: 'Categories', icon: 'fa-folder', routeName: ROUTE_NAMES.STORE_CATEGORIES },
      { name: 'Products', icon: 'fa-box', routeName: ROUTE_NAMES.STORE_PRODUCTS },
      { name: 'Profile', icon: 'fa-user', routeName: ROUTE_NAMES.PROFILE },
    ];
  }

  // MANAGER
  if (isManager.value) {
    return [
      { name: 'Dashboard', icon: 'fa-chart-bar', routeName: ROUTE_NAMES.STORE_DASHBOARD },
      { name: 'Employees', icon: 'fa-users', routeName: ROUTE_NAMES.STORE_EMPLOYEES },
      { name: 'Categories', icon: 'fa-folder', routeName: ROUTE_NAMES.STORE_CATEGORIES },
      { name: 'Products', icon: 'fa-box', routeName: ROUTE_NAMES.STORE_PRODUCTS },
      { name: 'Profile', icon: 'fa-user', routeName: ROUTE_NAMES.PROFILE },
    ];
  }

  // STAFF
  if (isStaff.value) {
    return [
      { name: 'Dashboard', icon: 'fa-chart-bar', routeName: ROUTE_NAMES.STORE_DASHBOARD },
      { name: 'Products', icon: 'fa-box', routeName: ROUTE_NAMES.STORE_PRODUCTS },
      { name: 'Profile', icon: 'fa-user', routeName: ROUTE_NAMES.PROFILE },
    ];
  }

  // SUPER_ADMIN
  if (isSuperAdmin.value) {
    return [
      { name: 'All Stores', icon: 'fa-store', routeName: ROUTE_NAMES.DASHBOARD_ADMIN },
      { name: 'Profile', icon: 'fa-user', routeName: ROUTE_NAMES.PROFILE },
    ];
  }

  return [];
});

const isActive = (item) => route.name === item.routeName;

const navigate = (item) => {
  router.push({ name: item.routeName, params: item.params || {} });
};

const handleLogout = async () => {
  await authStore.logout();
  router.push({ name: ROUTE_NAMES.LOGIN });
};

const goBackToMyStores = () => {
  storeStore.setActiveStore(null);
  router.push({ name: ROUTE_NAMES.OWNER_STORES });
};

</script>

<style scoped></style>