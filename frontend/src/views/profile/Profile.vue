<template>
    <component :is="isCustomer ? CustomerLayout : DashboardLayout">

        <!-- Header -->
        <div class="mb-6 px-8 pt-6">
            <h1 class="text-2xl font-bold text-gray-900">Profile</h1>
            <p class="text-sm text-gray-500 mt-0.5">Your account information</p>
        </div>

        <div class="max-w-3xl space-y-4 px-8">

            <!-- User info card -->
            <div class="bg-white rounded-xl border border-gray-200 p-6">
                <h2 class="text-base font-semibold text-gray-900 mb-4">Account Info</h2>
                <div class="space-y-4 text-sm">

                    <!-- Full Name -->
                    <div>
                        <div class="flex items-center gap-2 mb-1">
                            <label class="text-gray-400">Full Name</label>
                            <span class="text-xs px-2 py-0.5 bg-gray-100 text-gray-400 rounded-full">Coming soon</span>
                        </div>
                        <input disabled :value="userName"
                            class="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg bg-gray-50 text-gray-400 cursor-not-allowed" />
                    </div>

                    <!-- Email -->
                    <div>
                        <div class="flex items-center gap-2 mb-1">
                            <label class="text-gray-400">Email</label>
                            <span class="text-xs px-2 py-0.5 bg-gray-100 text-gray-400 rounded-full">Coming soon</span>
                        </div>
                        <input disabled :value="userEmail"
                            class="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg bg-gray-50 text-gray-400 cursor-not-allowed" />
                    </div>

                    <!-- Role -->
                    <div>
                        <p class="text-gray-400 mb-1">Role</p>
                        <span class="text-xs px-2 py-0.5 rounded-full font-medium" :class="roleBadgeClass">
                            {{ userRole }}
                        </span>
                    </div>

                    <!-- Email Verified -->
                    <div>
                        <p class="text-gray-400 mb-1">Email Verified</p>
                        <span v-if="user?.emailVerified" class="text-green-600 text-sm">
                            <i class="fas fa-check-circle mr-1"></i>Verified
                        </span>
                        <span v-else class="text-gray-400 text-sm">
                            <i class="fas fa-times-circle mr-1"></i>Not verified
                        </span>
                    </div>

                </div>
            </div>

            <!-- Change password card -->
            <div class="bg-white rounded-xl border border-gray-200 p-6 mb-6">
                <div class="flex items-center gap-2 mb-4">
                    <h2 class="text-base font-semibold text-gray-900">Change Password</h2>
                    <span class="text-xs px-2 py-0.5 bg-gray-100 text-gray-400 rounded-full">Coming soon</span>
                </div>

                <div class="space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-400 mb-1">Current Password</label>
                        <input disabled type="password" placeholder="Enter current password"
                            class="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg bg-gray-50 text-gray-400 cursor-not-allowed" />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-400 mb-1">New Password</label>
                        <input disabled type="password" placeholder="Min 8 characters"
                            class="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg bg-gray-50 text-gray-400 cursor-not-allowed" />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-400 mb-1">Confirm New Password</label>
                        <input disabled type="password" placeholder="Repeat new password"
                            class="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg bg-gray-50 text-gray-400 cursor-not-allowed" />
                    </div>

                    <button disabled
                        class="px-4 py-2 text-sm font-medium text-white bg-blue-300 rounded-lg cursor-not-allowed"
                        title="Coming soon">
                        Change Password
                    </button>
                </div>
            </div>

        </div>

    </component>
</template>

<script setup>
import { computed } from 'vue';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import { useAuthStore } from '../../stores/auth';
import { ROLES } from '../../utils/constants';

const authStore = useAuthStore();
const { user, userName, userEmail, userRole } = storeToRefs(authStore);

const isCustomer = computed(() => userRole.value === ROLES.CUSTOMER);

const roleBadgeClass = computed(() => {
    const map = {
        OWNER: 'bg-blue-100 text-blue-700',
        ADMIN: 'bg-sky-100 text-sky-700',
        MANAGER: 'bg-green-100 text-green-700',
        STAFF: 'bg-emerald-100 text-emerald-700',
        SUPER_ADMIN: 'bg-indigo-100 text-indigo-700',
        CUSTOMER: 'bg-gray-100 text-gray-700',
    };
    return map[userRole.value] || 'bg-gray-100 text-gray-700';
});
</script>