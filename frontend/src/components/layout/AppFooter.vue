<template>
    <footer class="bg-white border-t border-gray-200 mt-auto">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                <!-- About -->
                <div>
                    <h3 class="text-base font-bold text-blue-600 mb-3">RetailHub</h3>
                    <p class="text-gray-500 text-sm">
                        Multi-Store Retail Management Platform. Manage your stores, inventory, and customers in one
                        place.
                    </p>
                </div>

                <!-- Quick Links -->
                <div>
                    <h3 class="text-sm font-semibold text-gray-900 mb-3">Quick Links</h3>
                    <ul class="space-y-2 text-sm">
                        <template v-if="isAuthenticated">
                            <li>
                                <button @click="router.push({ name: ROUTE_NAMES.EXPLORE })"
                                    class="text-gray-500 hover:text-blue-600 transition-colors">
                                    Explore Products
                                </button>
                            </li>
                        </template>

                        <template v-else>
                            <li>
                                <button @click="router.push({ name: ROUTE_NAMES.LOGIN })"
                                    class="text-gray-500 hover:text-blue-600 transition-colors">
                                    Login
                                </button>
                            </li>
                            <li>
                                <button @click="router.push({ name: 'register' })"
                                    class="text-gray-500 hover:text-blue-600 transition-colors">
                                    Register
                                </button>
                            </li>
                        </template>
                    </ul>
                </div>

                <!-- Contact -->
                <div>
                    <h3 class="text-sm font-semibold text-gray-900 mb-3">Platform</h3>
                    <ul class="space-y-2 text-sm text-gray-500">
                        <li>Inventory Tracking</li>
                        <li>Order Management</li>
                    </ul>
                </div>
            </div>

            <!-- Copyright -->
            <div class="mt-8 pt-6 border-t border-gray-200 text-center text-xs text-gray-400">
                <p>&copy; 2026 RetailHub. All rights reserved.</p>
            </div>
        </div>
    </footer>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ROUTE_NAMES } from '../../utils/constants';
import { useAuthStore } from '../../stores/auth';
import { storeToRefs } from 'pinia';

const router = useRouter();
const authStore = useAuthStore();

const { isAuthenticated } = storeToRefs(authStore);

</script>