<template>
    <div class="min-h-screen bg-gray-100">
        <!-- Header -->
        <header class="bg-white shadow">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
                <h1 class="text-2xl font-bold text-gray-900">Profile</h1>
                <div class="flex gap-3">
                    <button @click="goToDashboard"
                        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                        Dashboard
                    </button>
                    <button @click="handleLogout"
                        class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                        Logout
                    </button>
                </div>
            </div>
        </header>

        <!-- Main Content -->
        <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div class="bg-white rounded-lg shadow p-6">
                <h2 class="text-xl font-semibold mb-6">User Profile</h2>

                <div class="space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700">Name</label>
                        <p class="mt-1 text-lg text-gray-900">{{ userName }}</p>
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700">Email</label>
                        <p class="mt-1 text-lg text-gray-900">{{ userEmail }}</p>
                    </div>

                    <div>
                        <label class="block text-sm font-medium text-gray-700">Role</label>
                        <p class="mt-1">
                            <span
                                class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-blue-100 text-blue-800">
                                {{ userRole }}
                            </span>
                        </p>
                    </div>
                </div>

                <div class="mt-8 p-4 bg-gray-50 rounded-lg">
                    <h3 class="font-semibold text-gray-900 mb-2">Full User Data:</h3>
                    <pre class="text-sm text-gray-700 overflow-x-auto">{{ user }}</pre>
                </div>

                <div class="mt-6">
                    <p class="text-gray-500 italic">
                        Profile editing features coming soon...
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
const { user, userName, userEmail, userRole } = storeToRefs(authStore);
const router = useRouter();

const handleLogout = async () => {
    await authStore.logout();
    router.push({ name: ROUTE_NAMES.LOGIN });
};

const goToDashboard = () => {
    router.push({ name: ROUTE_NAMES.DASHBOARD });
};
</script>

<style scoped></style>