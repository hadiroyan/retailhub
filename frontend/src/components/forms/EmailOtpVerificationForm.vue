<template>
    <div class="space-y-2" :class="{ 'flex flex-col items-center text-center': centered }">
        <div v-if="!showForm" class="mt-1">
            <button @click="handleSend" :disabled="loading"
                class="px-3 py-1.5 text-xs font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                <span v-if="loading"><i class="fas fa-spinner animate-spin mr-1"></i>Sending...</span>
                <span v-else>Send OTP</span>
            </button>
        </div>

        <div v-if="showForm" class="space-y-2 mt-1" :class="{ 'w-full': centered }">
            <p class="text-xs text-gray-500">
                OTP sent to <span class="font-medium">{{ email }}</span>. Valid for 10 minutes.
            </p>
            <div class="flex gap-2 flex-wrap" :class="centered ? 'justify-center' : ''">
                <input v-model="otpInput" type="text" maxlength="6" placeholder="Enter 6-digit OTP"
                    class="w-40 px-3 py-1.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                <button @click="handleVerify" :disabled="loading || otpInput.length !== 6"
                    class="px-3 py-1.5 text-xs font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                    <span v-if="loading"><i class="fas fa-spinner animate-spin mr-1"></i></span>
                    <span v-else>Verify</span>
                </button>
                <button @click="handleSend" :disabled="loading"
                    class="px-3 py-1.5 text-xs font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors disabled:opacity-50">
                    Resend
                </button>
            </div>

            <div v-if="errorMsg" class="text-xs text-red-500">
                <i class="fas fa-times-circle mr-1"></i>{{ errorMsg }}
            </div>
            <div v-if="successMsg" class="text-xs text-green-600">
                <i class="fas fa-check-circle mr-1"></i>{{ successMsg }}
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../../stores/auth';

const props = defineProps({
    email: { type: String, required: true },
    autoSent: { type: Boolean, default: false },
    centered: { type: Boolean, default: false },
});
const emit = defineEmits(['verified']);

const authStore = useAuthStore();

const showForm = ref(props.autoSent);
const otpInput = ref('');
const loading = ref(false);
const errorMsg = ref(null);
const successMsg = ref(null);

const handleSend = async () => {
    errorMsg.value = null;
    successMsg.value = null;
    loading.value = true;
    try {
        await authStore.resendOtp();
        showForm.value = true;
        successMsg.value = 'OTP sent! Check your email.';
    } catch (err) {
        errorMsg.value = err.response?.data?.message || 'Failed to send OTP';
    } finally {
        loading.value = false;
    }
};

const handleVerify = async () => {
    errorMsg.value = null;
    successMsg.value = null;
    loading.value = true;
    try {
        await authStore.verifyEmail(otpInput.value);
        successMsg.value = 'Email verified successfully!';
        otpInput.value = '';
        emit('verified');
    } catch (err) {
        errorMsg.value = err.response?.data?.message || 'Invalid or expired OTP';
    } finally {
        loading.value = false;
    }
};
</script>