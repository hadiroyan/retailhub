<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ROUTE_NAMES } from '@/utils/constants';

const router = useRouter();
const mobileMenuOpen = ref(false);

const toggleMobileMenu = () => {
    mobileMenuOpen.value = !mobileMenuOpen.value;
};

const navigateTo = (routeName) => {
    router.push({ name: routeName });
    mobileMenuOpen.value = false;
};
</script>

<template>
    <div class="min-h-screen flex flex-col bg-gray-50">
        <!-- Navbar -->
        <nav class="bg-white shadow-md sticky top-0 z-50">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div class="flex justify-between items-center h-16">
                    <!-- Logo/Brand -->
                    <div class="flex items-center cursor-pointer" @click="navigateTo(ROUTE_NAMES.HOME)">
                        <h1 class="text-2xl font-bold text-blue-600">RetailHub</h1>
                    </div>

                    <!-- Desktop Navigation -->
                    <div class="hidden md:flex items-center space-x-4">
                        <button @click="navigateTo(ROUTE_NAMES.HOME)"
                            class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors">
                            Home
                        </button>
                        <button @click="navigateTo(ROUTE_NAMES.LOGIN)"
                            class="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors">
                            Login
                        </button>
                        <button @click="navigateTo('register')"
                            class="bg-blue-600 text-white hover:bg-blue-700 px-4 py-2 rounded-md text-sm font-medium transition-colors">
                            Register
                        </button>
                    </div>

                    <!-- Mobile menu button -->
                    <div class="md:hidden">
                        <button @click="toggleMobileMenu"
                            class="text-gray-700 hover:text-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-md p-2">
                            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path v-if="!mobileMenuOpen" stroke-linecap="round" stroke-linejoin="round"
                                    stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                                <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Mobile menu -->
            <div v-if="mobileMenuOpen" class="md:hidden border-t border-gray-200">
                <div class="px-2 pt-2 pb-3 space-y-1">
                    <button @click="navigateTo(ROUTE_NAMES.HOME)"
                        class="block w-full text-left text-gray-700 hover:bg-gray-100 hover:text-blue-600 px-3 py-2 rounded-md text-base font-medium transition-colors">
                        Home
                    </button>
                    <button @click="navigateTo(ROUTE_NAMES.LOGIN)"
                        class="block w-full text-left text-gray-700 hover:bg-gray-100 hover:text-blue-600 px-3 py-2 rounded-md text-base font-medium transition-colors">
                        Login
                    </button>
                    <button @click="navigateTo('register')"
                        class="block w-full text-left bg-blue-600 text-white hover:bg-blue-700 px-3 py-2 rounded-md text-base font-medium transition-colors">
                        Register
                    </button>
                </div>
            </div>
        </nav>

        <!-- Main Content Area -->
        <main class="flex-1">
            <slot></slot>
        </main>

        <!-- Footer -->
        <footer class="bg-white border-t border-gray-200 mt-auto">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                    <!-- About -->
                    <div>
                        <h3 class="text-lg font-semibold text-gray-900 mb-3">RetailHub</h3>
                        <p class="text-gray-600 text-sm">
                            Multi-Store Retail Management Platform. Manage your stores, inventory, and customers in one
                            place.
                        </p>
                    </div>

                    <!-- Quick Links -->
                    <div>
                        <h3 class="text-lg font-semibold text-gray-900 mb-3">Quick Links</h3>
                        <ul class="space-y-2 text-sm">
                            <li>
                                <button @click="navigateTo(ROUTE_NAMES.HOME)"
                                    class="text-gray-600 hover:text-blue-600 transition-colors">
                                    Home
                                </button>
                            </li>
                            <li>
                                <button @click="navigateTo(ROUTE_NAMES.LOGIN)"
                                    class="text-gray-600 hover:text-blue-600 transition-colors">
                                    Login
                                </button>
                            </li>
                            <li>
                                <button @click="navigateTo('register')"
                                    class="text-gray-600 hover:text-blue-600 transition-colors">
                                    Register
                                </button>
                            </li>
                        </ul>
                    </div>

                    <!-- Contact -->
                    <div>
                        <h3 class="text-lg font-semibold text-gray-900 mb-3">Contact</h3>
                        <ul class="space-y-2 text-sm text-gray-600">
                            <li>Email: support@retailhub.com</li>
                            <li>Phone: +62 123 4567 890</li>
                            <li>Jakarta, Indonesia</li>
                        </ul>
                    </div>
                </div>

                <!-- Copyright -->
                <div class="mt-8 pt-8 border-t border-gray-200 text-center text-sm text-gray-600">
                    <p>&copy; 2026 RetailHub. All rights reserved.</p>
                </div>
            </div>
        </footer>
    </div>
</template>

<style scoped></style>