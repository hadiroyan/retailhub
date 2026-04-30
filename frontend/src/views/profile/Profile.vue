<template>
    <component :is="isCustomer ? CustomerLayout : DashboardLayout">

        <div class="flex flex-col items-center">
            <!-- Header -->
            <div class="mb-6 px-8 pt-6 text-center">
                <h1 class="text-2xl font-bold text-gray-900">Profile</h1>
                <p class="text-sm text-gray-500 mt-0.5">Your account information</p>
            </div>

            <div class="max-w-6xl grid grid-cols-1 lg:grid-cols-3 gap-6 px-8">
                <!-- User info card -->
                <div class="lg:col-span-2 bg-white rounded-xl border border-gray-200 p-6 mb-6">
                    <h2 class="text-base font-semibold text-gray-900 mb-4">Account Info</h2>
                    <div class="space-y-4 text-sm">

                        <!-- Full Name -->
                        <div>
                            <label class="block text-gray-700 font-medium mb-1">Full Name <span
                                    class="text-red-500">*</span></label>
                            <input v-model="profileForm.fullName" type="text" placeholder="Your full name"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>

                        <!-- Email — readonly -->
                        <div>
                            <label class="block text-gray-400 mb-1">Email</label>
                            <input disabled :value="userEmail"
                                class="w-full px-3 py-2 text-sm border border-gray-200 rounded-lg bg-gray-50 text-gray-400 cursor-not-allowed" />
                            <p class="text-xs text-gray-400 mt-1">Email cannot be changed</p>
                        </div>

                        <!-- Phone -->
                        <div>
                            <label class="block text-gray-700 font-medium mb-1">Phone</label>
                            <input v-model="profileForm.phone" type="text" placeholder="e.g. 08123456789"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>

                        <!-- Address -->
                        <div>
                            <label class="block text-gray-700 font-medium mb-1">Address</label>
                            <textarea v-model="profileForm.address" rows="3" placeholder="Your full address"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"></textarea>
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

                        <!-- Error / Success -->
                        <div v-if="profileError"
                            class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                            {{ profileError }}
                        </div>
                        <div v-if="profileSuccess"
                            class="p-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-600">
                            <i class="fas fa-check-circle mr-1"></i>Profile updated successfully!
                        </div>

                        <button @click="handleUpdateProfile" :disabled="profileLoading"
                            class="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                            <span v-if="profileLoading"><i class="fas fa-spinner animate-spin mr-1"></i>Saving...</span>
                            <span v-else>Save Changes</span>
                        </button>
                    </div>
                </div>

                <!-- Change password card -->
                <div class="lg:col-span-1 bg-white rounded-xl border border-gray-200 p-6 mb-6">
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
        </div>
    </component>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
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

// Profile form
const profileForm = ref({
    fullName: '',
    phone: '',
    address: '',
});
const profileLoading = ref(false);
const profileError = ref(null);
const profileSuccess = ref(false);

// Pre-fill form dari user data
onMounted(() => {
    if (user.value) {
        profileForm.value = {
            fullName: user.value.fullName || '',
            phone: user.value.phone || '',
            address: user.value.address || '',
        };
    }
});

const handleUpdateProfile = async () => {
    profileError.value = null;
    profileSuccess.value = false;

    if (!profileForm.value.fullName.trim()) {
        profileError.value = 'Full name is required';
        return;
    }

    profileLoading.value = true;
    try {
        await authStore.updateProfile(profileForm.value);
        profileSuccess.value = true;
        setTimeout(() => { profileSuccess.value = false; }, 3000);
    } catch (err) {
        profileError.value = err.response?.data?.message || 'Failed to update profile';
    } finally {
        profileLoading.value = false;
    }
};
</script>