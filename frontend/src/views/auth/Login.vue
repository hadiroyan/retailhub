<template>
    <AuthLayout>
        <div class="space-y-6">
            <!-- Title -->
            <div class="text-center">
                <h2 class="text-3xl font-bold text-gray-900">Welcome Back</h2>
                <p class="mt-2 text-sm text-gray-600">
                    Sign in to your account to continue
                </p>
            </div>

            <!-- Login Form -->
            <form @submit.prevent="handleLogin" class="space-y-4">
                <!-- Email Input -->
                <Input v-model="email" type="email" label="Email Address" placeholder="you@example.com"
                    :error="emailError" required @blur="validateEmail" />

                <!-- Password Input -->
                <div class="relative">
                    <Input v-model="password" :type="showPassword ? 'text' : 'password'" label="Password"
                        placeholder="Enter your password" :error="passwordError" required @blur="validatePassword" />

                    <span class="absolute right-3 top-7 p-1 cursor-pointer z-10" @click="showPassword = !showPassword">
                        <i :class="['fas', showPassword ? 'fa-eye' : 'fa-eye-slash']"></i>
                    </span>
                </div>

                <!-- Error Message from Store -->
                <div v-if="authStore.error" class="p-3 bg-red-50 border border-red-200 rounded-lg">
                    <p class="text-sm text-red-600">{{ authStore.error }}</p>
                </div>

                <!-- Forgot Password Link -->
                <div class="text-right">
                    <a href="#" class="text-sm text-blue-600 hover:text-blue-700 hover:underline">
                        Forgot password?
                    </a>
                </div>

                <!-- Submit Button -->
                <Button type="submit" variant="primary" size="lg" full-width :loading="authStore.loading">
                    {{ authStore.loading ? 'Signing in...' : 'Sign In' }}
                </Button>
            </form>

            <!-- Divider -->
            <div class="relative">
                <div class="absolute inset-0 flex items-center">
                    <div class="w-full border-t border-gray-300"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                    <span class="px-2 bg-white text-gray-500">Don't have an account?</span>
                </div>
            </div>

            <!-- Register Link -->
            <Button @click="goToRegister" variant="outline" size="lg" full-width>
                Create an Account
            </Button>
        </div>
    </AuthLayout>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';
import { ROUTE_NAMES } from '../../utils/constants';
import AuthLayout from '../../layouts/AuthLayout.vue';
import Button from '../../components/common/BaseButton.vue';
import Input from '../../components/common/BaseInput.vue';

const router = useRouter();
const authStore = useAuthStore();

// Form data
const email = ref('');
const password = ref('');

// Form validation errors
const emailError = ref('');
const passwordError = ref('');

// Password visibility toggle
const showPassword = ref(false);

// Validation functions
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

    if (password.value.length < 6) {
        passwordError.value = 'Password must be at least 6 characters';
        return false;
    }

    passwordError.value = '';
    return true;
};

const validateForm = () => {
    const isEmailValid = validateEmail();
    const isPasswordValid = validatePassword();
    return isEmailValid && isPasswordValid;
};

// Handle login
const handleLogin = async () => {
    // Clear previous errors
    authStore.clearError();

    // Validate form
    if (!validateForm()) {
        return;
    }

    try {
        await authStore.login({
            email: email.value,
            password: password.value,
        });

        // Redirect to dashboard on success
        router.push({ name: ROUTE_NAMES.DASHBOARD });
    } catch (error) {
        console.error('Login failed:', error);
    }
};

// Navigate to register
const goToRegister = () => {
    router.push({ name: 'register' });
};
</script>

<style scoped></style>