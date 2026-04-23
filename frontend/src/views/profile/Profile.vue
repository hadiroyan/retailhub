<template>
    <DashboardLayout>
        <!-- Header -->
        <div class="mb-6">
            <h1 class="text-2xl font-bold text-gray-900">Profile</h1>
            <p class="text-sm text-gray-500 mt-0.5">Your account information</p>
        </div>

        <div class="max-w-2xl space-y-4">

            <!-- User info card -->
            <div class="bg-white rounded-xl border border-gray-200 p-6">
                <h2 class="text-base font-semibold text-gray-900 mb-4">Account Info</h2>
                <div class="space-y-4 text-sm">
                    <div>
                        <p class="text-gray-400 mb-0.5">Full Name</p>
                        <p class="font-medium text-gray-800">{{ userName }}</p>
                    </div>
                    <div>
                        <p class="text-gray-400 mb-0.5">Email</p>
                        <p class="font-medium text-gray-800">{{ userEmail }}</p>
                    </div>
                    <div>
                        <p class="text-gray-400 mb-0.5">Role</p>
                        <span class="text-xs px-2 py-0.5 rounded-full font-medium" :class="roleBadgeClass">
                            {{ userRole }}
                        </span>
                    </div>
                    <div>
                        <p class="text-gray-400 mb-0.5">Email Verified</p>
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
            <div class="bg-white rounded-xl border border-gray-200 p-6">
                <h2 class="text-base font-semibold text-gray-900 mb-4">Change Password</h2>

                <div class="space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Current Password</label>
                        <input v-model="passwordForm.currentPassword" type="password"
                            placeholder="Enter current password"
                            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">New Password</label>
                        <input v-model="passwordForm.newPassword" type="password" placeholder="Min 8 characters"
                            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Confirm New Password</label>
                        <input v-model="passwordForm.confirmPassword" type="password" placeholder="Repeat new password"
                            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                    </div>

                    <div v-if="passwordError"
                        class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                        {{ passwordError }}
                    </div>
                    <div v-if="passwordSuccess"
                        class="p-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-600">
                        Password changed successfully!
                    </div>

                    <button @click="handleChangePassword" :disabled="passwordLoading"
                        class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                        <span v-if="passwordLoading"><i class="fas fa-spinner fa-spin mr-1"></i>Saving...</span>
                        <span v-else>Change Password</span>
                    </button>
                </div>
            </div>

        </div>

    </DashboardLayout>
</template>

<script setup>
import { ref, computed } from 'vue';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import { useAuthStore } from '../../stores/auth';
import { isStrongPassword } from '../../utils/helper';

const authStore = useAuthStore();
const { user, userName, userEmail, userRole } = storeToRefs(authStore);

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

const passwordForm = ref({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
});
const passwordLoading = ref(false);
const passwordError = ref(null);
const passwordSuccess = ref(false);

const handleChangePassword = async () => {
    passwordError.value = null;
    passwordSuccess.value = false;

    if (!passwordForm.value.currentPassword || !passwordForm.value.newPassword) {
        passwordError.value = 'All fields are required';
        return;
    }
    if (!isStrongPassword(passwordForm.value.newPassword)) {
        passwordError.value = 'Password must be at least 8 characters with uppercase, lowercase, and number';
        return;
    }
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        passwordError.value = 'New passwords do not match';
        return;
    }

    passwordLoading.value = true;
    try {
        // TODO: call change password API when available
        await new Promise(resolve => setTimeout(resolve, 800));
        passwordSuccess.value = true;
        passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
    } catch (err) {
        passwordError.value = err.response?.data?.message || 'Failed to change password';
    } finally {
        passwordLoading.value = false;
    }
};
</script>