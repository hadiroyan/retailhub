<template>
    <AuthLayout>
        <div class="space-y-6 text-center">
            <div>
                <i class="fa-solid fa-envelope-circle-check text-blue-500 text-4xl mb-4"></i>
                <h2 class="text-2xl font-bold text-gray-900">Verify Your Email</h2>
                <p class="mt-2 text-sm text-gray-600">
                    We've sent a 6-digit code to <span class="font-medium">{{ userEmail }}</span>
                </p>
            </div>

            <EmailOtpVerificationForm :email="userEmail" auto-sent centered @verified="handleVerified" />

            <p class="text-xs text-gray-400">
                Wrong email? You can
                <button @click="goHome" class="text-blue-600 h over:underline">go back</button>.
            </p>
        </div>
    </AuthLayout>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '../../stores/auth';
import { ROUTE_NAMES } from '../../utils/constants';
import AuthLayout from '../../layouts/AuthLayout.vue';
import EmailOtpVerificationForm from '../../components/forms/EmailOtpVerificationForm.vue';

const router = useRouter();
const authStore = useAuthStore();
const { userEmail } = storeToRefs(authStore);

const handleVerified = () => {
    setTimeout(() => {
        router.push({ name: ROUTE_NAMES.DASHBOARD });
    }, 1200);
};

const goHome = () => {
    router.push({ name: ROUTE_NAMES.DASHBOARD });
};
</script>