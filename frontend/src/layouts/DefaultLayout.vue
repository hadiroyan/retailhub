<template>
    <div class="min-h-screen flex flex-col bg-gray-50">

        <!-- Navbar -->
        <header class="bg-white border-b border-gray-200 h-16 flex items-center px-4 sticky top-0 z-50 shadow-sm">
            <div class="max-w-7xl mx-auto w-full flex items-center justify-between">

                <!-- Left: Brand -->
                <span class="text-xl font-bold text-blue-600 cursor-pointer"
                    @click="router.push({ name: ROUTE_NAMES.HOME })">
                    RetailHub
                </span>

                <!-- Desktop nav -->
                <div class="hidden md:flex items-center gap-2">
                    <button @click="router.push({ name: ROUTE_NAMES.HOME })"
                        class="text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                        Home
                    </button>
                    <button @click="router.push({ name: ROUTE_NAMES.EXPLORE })"
                        class="text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                        Explore
                    </button>
                    <button @click="router.push({ name: ROUTE_NAMES.LOGIN })"
                        class="text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                        Login
                    </button>
                    <button @click="router.push({ name: 'register' })"
                        class="text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg transition-colors">
                        Register
                    </button>
                </div>

                <!-- Mobile menu button -->
                <button @click="toggleMobileMenu" class="md:hidden p-2 text-gray-500 hover:text-blue-600 rounded-lg">
                    <i :class="['fas', mobileMenuOpen ? 'fa-times' : 'fa-bars']"></i>
                </button>

            </div>
        </header>

        <!-- Mobile menu -->
        <div v-if="mobileMenuOpen" class="md:hidden bg-white border-b border-gray-200 px-4 py-3 space-y-1">
            <button @click="navigate(ROUTE_NAMES.HOME)"
                class="block w-full text-left text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                Home
            </button>
            <button @click="navigate(ROUTE_NAMES.EXPLORE)"
                class="block w-full text-left text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                Explore
            </button>
            <button @click="navigate(ROUTE_NAMES.LOGIN)"
                class="block w-full text-left text-sm text-gray-600 hover:text-blue-600 px-3 py-2 rounded-lg transition-colors">
                Login
            </button>
            <button @click="navigate('register')"
                class="block w-full text-left text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 px-3 py-2 rounded-lg transition-colors">
                Register
            </button>
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
import AppFooter from '../components/layout/AppFooter.vue';
import { ROUTE_NAMES } from '../utils/constants';

const router = useRouter();
const mobileMenuOpen = ref(false);

const toggleMobileMenu = () => {
    mobileMenuOpen.value = !mobileMenuOpen.value;
};

const navigate = (routeName) => {
    router.push({ name: routeName });
    mobileMenuOpen.value = false;
};
</script>

<style scoped></style>