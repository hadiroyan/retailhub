<template>
    <AuthLayout>
        <div class="space-y-6">
            <!-- Success Message -->
            <div v-if="registrationSuccess" class="p-4 bg-green-50 border border-green-200 rounded-lg">
                <div class="flex items-center">
                    <i class="fa-solid fa-circle-check text-green-500 text-xl mr-3"></i>
                    <div>
                        <p class="text-sm font-medium text-green-800">Registration Successful!</p>
                        <p class="text-xs text-green-700 mt-1">Redirecting to dashboard page...</p>
                    </div>
                </div>
            </div>

            <!-- Title -->
            <div class="text-center">
                <h2 class="text-3xl font-bold text-gray-900">Create Customer Account</h2>
                <p class="mt-2 text-sm text-gray-600">
                    Join RetailHub and start shopping
                </p>
            </div>

            <!-- Registration Form -->
            <form @submit.prevent="handleRegister" class="space-y-4">
                <!-- Full Name Input -->
                <Input v-model="fullName" type="text" label="Full Name" placeholder="Your name" :error="fullNameError"
                    required @blur="validateFullName" />

                <!-- Email Input -->
                <Input v-model="email" type="email" label="Email Address" placeholder="you@example.com"
                    :error="emailError" required @blur="validateEmail" />

                <!-- Password Input -->
                <div class="relative">
                    <Input v-model="password" :type="showPassword ? 'text' : 'password'" label="Password"
                        placeholder="Create a strong password" :error="passwordError"
                        help-text="Min 8 characters (max 128)" required @blur="validatePassword" />

                    <span class="absolute right-3 top-7 p-1 cursor-pointer z-10" @click="showPassword = !showPassword">
                        <i :class="['fas', showPassword ? 'fa-eye' : 'fa-eye-slash']"></i>
                    </span>
                </div>

                <!-- Confirm Password Input -->
                <div class="relative">
                    <Input v-model="confirmPassword" :type="showConfirmPassword ? 'text' : 'password'"
                        label="Confirm Password" placeholder="Re-enter your password" :error="confirmPasswordError"
                        required />

                    <span class="absolute right-3 top-7 p-1 cursor-pointer z-10"
                        @click="showConfirmPassword = !showConfirmPassword">
                        <i :class="['fas', showConfirmPassword ? 'fa-eye' : 'fa-eye-slash']"></i>
                    </span>
                </div>

                <!-- Error Message from Store -->
                <div v-if="authStore.error" class="p-3 bg-red-50 border border-red-200 rounded-lg">
                    <p class="text-sm text-red-600">{{ authStore.error }}</p>
                </div>

                <!-- Submit Button -->
                <Button type="submit" variant="primary" size="lg" full-width :loading="authStore.loading"
                    :disabled="registrationSuccess">
                    {{ authStore.loading ? 'Creating Account...' : 'Create Account' }}
                </Button>
            </form>

            <!-- Back Button -->
            <Button @click="goBack" variant="outline" size="lg" full-width
                :disabled="authStore.loading || registrationSuccess">
                Back to Account Type
            </Button>

            <!-- Divider -->
            <div class="relative">
                <div class="absolute inset-0 flex items-center">
                    <div class="w-full border-t border-gray-300"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                    <span class="px-2 bg-white text-gray-500">Already have an account?</span>
                </div>
            </div>

            <!-- Login Link -->
            <button @click="goToLogin"
                class="w-full text-center text-blue-600 hover:text-blue-700 font-medium hover:underline"
                :disabled="authStore.loading || registrationSuccess">
                Sign in instead
            </button>
        </div>
    </AuthLayout>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { ROUTE_NAMES } from '../../utils/constants';
import AuthLayout from '../../layouts/AuthLayout.vue';
import Button from '../../components/common/BaseButton.vue';
import Input from '../../components/common/BaseInput.vue';

const router = useRouter();
const authStore = useAuthStore();

// Form data
const fullName = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');

// Form validation errors
const fullNameError = ref('');
const emailError = ref('');
const passwordError = ref('');

// Success state
const registrationSuccess = ref(false);

// Password visibility toggle
const showPassword = ref(false);
const showConfirmPassword = ref(false);

// Validation functions
const validateFullName = () => {
    if (!fullName.value) {
        fullNameError.value = 'Full name is required';
        return false;
    }
    if (fullName.value.length < 2) {
        fullNameError.value = 'Full name must be at least 2 characters';
        return false;
    }
    if (fullName.value.length > 255) {
        fullNameError.value = 'Full name must be at most 255 characters';
        return false;
    }
    fullNameError.value = '';
    return true;
};

const validateEmail = () => {
    if (!email.value) {
        emailError.value = 'Email is required';
        return false;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email.value)) {
        emailError.value = 'Please enter a valid email';
        return false;
    }
    emailError.value = '';
    return true;
};

const validatePassword = () => {
    if (!password.value) {
        passwordError.value = 'Password is required';
        return false;
    }
    if (password.value.length < 8) {
        passwordError.value = 'Password must be at least 8 characters';
        return false;
    }
    if (password.value.length > 128) {
        passwordError.value = 'Password must be at most 128 characters';
        return false;
    }
    passwordError.value = '';
    return true;
};

const confirmPasswordError = computed(() => {
    if (!confirmPassword.value) return '';
    if (confirmPassword.value !== password.value) {
        return 'Passwords do not match';
    }
    return '';
});

const validateForm = () => {
    const isFullNameValid = validateFullName();
    const isEmailValid = validateEmail();
    const isPasswordValid = validatePassword();
    const isConfirmPasswordValid = !confirmPasswordError.value;

    return isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid;
};

// Handle registration
const handleRegister = async () => {
    authStore.clearError();

    if (!validateForm()) {
        return;
    }

    try {
        await authStore.registerCustomer({
            fullName: fullName.value,
            email: email.value,
            password: password.value,
        });

        // Show success message
        registrationSuccess.value = true;

        // Redirect to dashboard after 2 seconds
        setTimeout(() => {
            router.push({ name: ROUTE_NAMES.DASHBOARD });
        }, 2000);
    } catch (error) {
        console.error('Registration failed:', error);
    }
};

const goToLogin = () => {
    router.push({ name: ROUTE_NAMES.DASHBOARD });
};

const goBack = () => {
    router.push({ name: 'register' });
};
</script>

<style scoped></style>