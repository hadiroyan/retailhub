<template>
    <component :is="isCustomer ? CustomerLayout : DashboardLayout">

        <div class="flex flex-col items-center">
            <!-- Header -->
            <div class="mb-6 px-8 pt-6 text-center">
                <h1 class="text-2xl font-bold text-gray-900">Profile</h1>
                <p class="text-sm text-gray-500 mt-0.5">Your account information</p>
            </div>

            <div class="max-w-7xl grid grid-cols-1 lg:grid-cols-3 gap-6 px-8">

                <!-- Account Info card -->
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
                    <h2 class="text-base font-semibold text-gray-900 mb-4">Change Password</h2>
                    <div class="space-y-4">
                        <div class="relative">
                            <label class="block text-sm font-medium text-gray-700 mb-1">Current Password</label>

                            <input v-model="passwordForm.currentPassword"
                                :type="showCurrentPassword ? 'text' : 'password'" placeholder="Enter current password"
                                class="w-full px-3 py-2 pr-10 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                :class="{ 'border-red-400': passwordError }" />

                            <button type="button" class="absolute right-3 top-[31px] text-gray-500 hover:text-gray-700"
                                @click="showCurrentPassword = !showCurrentPassword">
                                <i :class="['fas', showCurrentPassword ? 'fa-eye' : 'fa-eye-slash']"></i>
                            </button>
                        </div>

                        <div class="relative">
                            <label class="block text-sm font-medium text-gray-700 mb-1">New Password</label>

                            <input v-model="passwordForm.newPassword" :type="showNewPassword ? 'text' : 'password'"
                                placeholder="Min 8 characters"
                                class="w-full px-3 py-2 pr-10 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                :class="{ 'border-red-400': passwordError }" />

                            <button type="button" class="absolute right-3 top-[31px] text-gray-500 hover:text-gray-700"
                                @click="showNewPassword = !showNewPassword">
                                <i :class="['fas', showNewPassword ? 'fa-eye' : 'fa-eye-slash']"></i>
                            </button>
                        </div>

                        <div class="relative">
                            <label class="block text-sm font-medium text-gray-700 mb-1">Confirm New Password</label>

                            <input v-model="passwordForm.confirmPassword"
                                :type="showConfirmNewPassword ? 'text' : 'password'" placeholder="Repeat new password"
                                class="w-full px-3 py-2 pr-10 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                :class="{ 'border-red-400': passwordError }" />

                            <button type="button" class="absolute right-3 top-[31px] text-gray-500 hover:text-gray-700"
                                @click="showConfirmNewPassword = !showConfirmNewPassword">
                                <i :class="['fas', showConfirmNewPassword ? 'fa-eye' : 'fa-eye-slash']"></i>
                            </button>
                        </div>

                        <div v-if="passwordError"
                            class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                            {{ passwordError }}
                        </div>

                        <button @click="openConfirmModal" :disabled="passwordLoading"
                            class="w-full px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                            <span v-if="passwordLoading"><i
                                    class="fas fa-spinner animate-spin mr-1"></i>Changing...</span>
                            <span v-else>Change Password</span>
                        </button>
                    </div>
                </div>

            </div>
        </div>

        <!-- Confirm change password modal -->
        <Teleport to="body">
            <div v-if="showConfirmModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-sm p-6 text-center">
                    <div class="w-14 h-14 rounded-full bg-yellow-100 flex items-center justify-center mx-auto mb-4">
                        <i class="fas fa-lock text-yellow-600 text-xl"></i>
                    </div>
                    <h3 class="text-lg font-semibold text-gray-900 mb-2">Change Password?</h3>
                    <p class="text-sm text-gray-500 mb-6">
                        You will be logged out after changing your password and need to login again.
                    </p>
                    <div class="flex gap-3">
                        <button @click="showConfirmModal = false"
                            class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                            Cancel
                        </button>
                        <button @click="handleChangePassword" :disabled="passwordLoading"
                            class="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                            <span v-if="passwordLoading"><i class="fas fa-spinner animate-spin mr-1"></i></span>
                            <span v-else>Yes, Change</span>
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

        <!-- Success modal — after password changed -->
        <Teleport to="body">
            <div v-if="showSuccessModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-sm p-6 text-center">
                    <div class="w-14 h-14 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-4">
                        <i class="fas fa-check text-green-600 text-xl"></i>
                    </div>
                    <h3 class="text-lg font-semibold text-gray-900 mb-2">Password Changed!</h3>
                    <p class="text-sm text-gray-500 mb-6">
                        Your password has been changed successfully. Please login again with your new password.
                    </p>
                    <button @click="handleSuccessConfirm"
                        class="w-full px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors">
                        Login Again
                    </button>
                </div>
            </div>
        </Teleport>

    </component>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import { useAuthStore } from '../../stores/auth';
import { ROLES, ROUTE_NAMES } from '../../utils/constants';
import authService from '../../services/authService';

const router = useRouter();
const authStore = useAuthStore();
const { user, userEmail, userRole } = storeToRefs(authStore);

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

// =========================================================================
// Change password
// =========================================================================
const passwordForm = ref({ currentPassword: '', newPassword: '', confirmPassword: '' });
const passwordLoading = ref(false);
const passwordError = ref(null);
const showConfirmModal = ref(false);
const showSuccessModal = ref(false);

const showCurrentPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmNewPassword = ref(false);

const openConfirmModal = () => {
    passwordError.value = null;

    if (!passwordForm.value.currentPassword) {
        passwordError.value = 'Current password is required';
        return;
    }
    if (!passwordForm.value.newPassword || passwordForm.value.newPassword.length < 8) {
        passwordError.value = 'New password must be at least 8 characters';
        return;
    }
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        passwordError.value = 'Passwords do not match';
        return;
    }

    showConfirmModal.value = true;
};

const handleChangePassword = async () => {
    passwordLoading.value = true;
    try {
        await authStore.changePassword(passwordForm.value);
        showConfirmModal.value = false;
        showSuccessModal.value = true;
        // Reset form
        passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
    } catch (err) {
        console.log(err);

        showConfirmModal.value = false;
        passwordError.value = err.response?.data?.message || 'Failed to change password';
    } finally {
        passwordLoading.value = false;
    }
};

const handleSuccessConfirm = async () => {
    showSuccessModal.value = false;
    await authStore.logout();
    router.push({ name: ROUTE_NAMES.LOGIN });
};
</script>