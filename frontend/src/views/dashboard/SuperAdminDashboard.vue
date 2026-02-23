<template>
    <div class="min-h-screen bg-gray-100">
        <!-- Header -->
        <header class="bg-white shadow">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
                <h1 class="text-2xl font-bold text-gray-900">Super Admin Dashboard</h1>
                <button @click="handleLogout"
                    class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                    Logout
                </button>
            </div>
        </header>

        <!-- Main Content -->
        <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div class="bg-white rounded-lg shadow p-6">
                <h2 class="text-xl font-semibold mb-4">Welcome, {{ userName }}! 👑</h2>
                <p class="text-gray-600 mb-4">
                    You are logged in as a <span class="font-semibold text-purple-600">Super Admin</span>
                </p>

                <div class="mt-6 p-4 bg-purple-50 rounded-lg">
                    <h3 class="font-semibold text-purple-900 mb-2">User Info:</h3>
                    <pre class="text-sm text-purple-800">{{ user }}</pre>
                </div>

                <div class="mt-6">
                    <p class="text-gray-500 italic">
                        Super Admin dashboard features coming soon...
                    </p>
                </div>
            </div>
        </main>
    </div>
</template>

<script setup>
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { ROUTE_NAMES } from '../../utils/constants';

const authStore = useAuthStore();
const { user, userName } = storeToRefs(authStore);
const router = useRouter();

const handleLogout = async () => {
    await authStore.logout();
    router.push({ name: ROUTE_NAMES.LOGIN });
};
</script>

<style scoped></style>