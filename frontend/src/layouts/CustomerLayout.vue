<template>
    <div class="min-h-screen flex flex-col bg-gray-50">
        <!-- Navbar -->
        <header class="bg-white border-b border-gray-200 h-16 flex items-center px-6 sticky top-0 z-50 shadow-sm">
            <div class="max-w-7xl mx-auto w-full flex items-center justify-between">

                <!-- Left: Brand + Explore -->
                <div class="flex items-center gap-6">
                    <span class="text-xl font-bold text-blue-600 cursor-pointer"
                        @click="router.push({ name: ROUTE_NAMES.HOME })">
                        RetailHub
                    </span>
                    <button @click="router.push({ name: ROUTE_NAMES.EXPLORE })"
                        class="hidden md:block text-sm text-gray-600 hover:text-blue-600 transition-colors">
                        Explore
                    </button>
                </div>

                <!-- Right: Cart + Auth -->
                <div class="hidden md:flex items-center gap-3">

                    <!-- Cart button dengan badge -->
                    <button @click="router.push({ name: ROUTE_NAMES.CART })"
                        class="relative p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                        <i class="fas fa-shopping-cart text-lg"></i>
                        <span v-if="totalItems > 0"
                            class="absolute -top-1 -right-1 w-5 h-5 bg-blue-600 text-white text-xs rounded-full flex items-center justify-center font-medium">
                            {{ totalItems > 99 ? '99+' : totalItems }}
                        </span>
                    </button>

                    <!-- Guest -->
                    <template v-if="!isAuthenticated">
                        <button @click="router.push({ name: ROUTE_NAMES.LOGIN })"
                            class="text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                            Login
                        </button>
                        <button @click="router.push({ name: 'register' })"
                            class="text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg transition-colors">
                            Register
                        </button>
                    </template>

                    <!-- Authenticated customer -->
                    <template v-else>
                        <button @click="router.push({ name: ROUTE_NAMES.ORDER_HISTORY })"
                            class="hidden md:block text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                            My Orders
                        </button>
                        <button @click="router.push({ name: ROUTE_NAMES.PROFILE })"
                            class="flex items-center gap-2 text-sm text-gray-700 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                            <i class="fas fa-user-circle text-lg"></i>
                            <span class="hidden md:inline">{{ userName }}</span>
                        </button>

                        <button @click="handleLogout"
                            class="px-3 py-2 text-sm text-white bg-red-500 hover:bg-red-600 rounded-lg transition-colors cursor-pointer">
                            <i class="fas fa-sign-out-alt"></i>
                            <span class="hidden md:inline ml-1">Logout</span>
                        </button>
                    </template>
                </div>
                <!-- Mobile menu button -->
                <button @click="toggleMobileMenu" class="md:hidden p-2 text-gray-500 hover:text-blue-600 rounded-lg">
                    <i :class="['fas', mobileMenuOpen ? 'fa-times' : 'fa-bars']"></i>
                </button>
            </div>
        </header>

        <!-- Mobile menu -->
        <div v-if="mobileMenuOpen" class="md:hidden bg-white border-b border-gray-200 px-4 py-3 space-y-1">
            <button @click="navigate(ROUTE_NAMES.EXPLORE)"
                class="block w-full text-left text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                Explore
            </button>
            <button @click="navigate(ROUTE_NAMES.CART)"
                class="block w-full text-left text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                Cart
                <span v-if="totalItems > 0" class="ml-1 text-xs text-blue-600 font-medium">({{ totalItems }})</span>
            </button>
            <template v-if="isAuthenticated">
                <button @click="navigate(ROUTE_NAMES.ORDER_HISTORY)"
                    class="block w-full text-left text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                    My Orders
                </button>
                <button @click="navigate(ROUTE_NAMES.PROFILE)"
                    class="block w-full text-left text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                    Profile
                </button>
                <button @click="handleLogout"
                    class="px-3 py-2 text-sm text-white bg-red-500 hover:bg-red-600 rounded-lg transition-colors cursor-pointer">
                    <i class="fas fa-sign-out-alt"></i>
                    <span class="hidden md:inline ml-1">Logout</span>
                </button>
            </template>
            <template v-else>
                <button @click="navigate(ROUTE_NAMES.LOGIN)"
                    class="block w-full text-left text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                    Login
                </button>
                <button @click="navigate('register')"
                    class="block w-full text-left text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 px-3 py-2 rounded-lg transition-colors">
                    Register
                </button>
            </template>
        </div>

        <!-- Main Content -->
        <main class="flex-1">
            <slot></slot>
        </main>

        <!-- Footer -->
        <AppFooter />
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import AppFooter from '../components/layout/AppFooter.vue';
import { useAuthStore } from '../stores/auth';
import { useCartStore } from '../stores/cartStore';
import { ROUTE_NAMES } from '../utils/constants';

const router = useRouter();
const authStore = useAuthStore();
const cartStore = useCartStore();

const { isAuthenticated, userName } = storeToRefs(authStore);
const { totalItems } = storeToRefs(cartStore);

const mobileMenuOpen = ref(false);

const toggleMobileMenu = () => {
    mobileMenuOpen.value = !mobileMenuOpen.value;
};

const navigate = (routeName) => {
    router.push({ name: routeName });
    mobileMenuOpen.value = false;
};

const handleLogout = async () => {
    await authStore.logout();
    router.push({ name: ROUTE_NAMES.LOGIN });
};
</script>

<style scoped></style>