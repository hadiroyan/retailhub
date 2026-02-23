<template>
    <DefaultLayout>
        <!-- Hero Section -->
        <section class="bg-linear-to-br from-blue-50 to-indigo-100 py-20">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div class="text-center">
                    <!-- Hero Title -->
                    <h1 class="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
                        Welcome to <span class="text-blue-600">RetailHub</span>
                    </h1>

                    <!-- Hero Subtitle -->
                    <p class="text-xl md:text-2xl text-gray-600 mb-8 max-w-3xl mx-auto">
                        Your All-in-One Multi-Store Retail Management Platform
                    </p>

                    <!-- Hero Description -->
                    <p class="text-lg text-gray-600 mb-10 max-w-2xl mx-auto">
                        Manage multiple stores, inventory, products, and customers seamlessly in one centralized
                        platform.
                    </p>

                    <!-- CTA Buttons -->
                    <div class="flex flex-col sm:flex-row gap-4 justify-center">
                        <Button @click="goToRegister" variant="primary" size="lg" class="px-8">
                            Get Started Free
                        </Button>
                        <Button @click="goToLogin" variant="outline" size="lg" class="px-8">
                            Sign In
                        </Button>
                    </div>
                </div>
            </div>
        </section>

        <!-- Features Section -->
        <section class="py-20 bg-white">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div class="text-center mb-16">
                    <h2 class="text-4xl font-bold text-gray-900 mb-4">
                        Why Choose RetailHub?
                    </h2>
                    <p class="text-xl text-gray-600">
                        Everything you need to run your retail business efficiently
                    </p>
                </div>

                <!-- Feature Grid -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                    <!-- Feature 1 -->
                    <div class="text-center p-6">
                        <div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <i class="fa-regular fa-building text-blue-600 text-3xl"></i>
                        </div>
                        <h3 class="text-xl font-semibold text-gray-900 mb-2">Multi-Store Management</h3>
                        <p class="text-gray-600">
                            Manage multiple store locations from a single dashboard
                        </p>
                    </div>

                    <!-- Feature 2 -->
                    <div class="text-center p-6">
                        <div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <i class="fa-solid fa-clipboard-list text-green-600 text-2xl"></i>
                        </div>
                        <h3 class="text-xl font-semibold text-gray-900 mb-2">Inventory Control</h3>
                        <p class="text-gray-600">
                            Real-time inventory tracking and management across all stores
                        </p>
                    </div>

                    <!-- Feature 3 -->
                    <div class="text-center p-6">
                        <div class="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <i class="fa-solid fa-chart-line text-purple-600 text-2xl"></i>
                        </div>
                        <h3 class="text-xl font-semibold text-gray-900 mb-2">Sales Analytics</h3>
                        <p class="text-gray-600">
                            Comprehensive analytics and insights to grow your business
                        </p>
                    </div>
                </div>
            </div>
        </section>

        <section class="py-20 bg-gray-50">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div class="text-center mb-16">
                    <h2 class="text-4xl font-bold text-gray-900 mb-4">
                        Perfect for Everyone
                    </h2>
                    <p class="text-xl text-gray-600">
                        Whether you're a store owner or a customer, RetailHub has you covered
                    </p>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">

                    <!-- For Customers -->
                    <div class="bg-white rounded-lg shadow-lg p-8">
                        <div class="flex items-center mb-4">
                            <div class="p-4 bg-green-100 rounded-full flex items-center justify-center mr-4">
                                <i class="fa-solid fa-cart-shopping text-green-600 text-2xl"></i>
                            </div>
                            <h3 class="text-2xl font-bold text-gray-900">For Customers</h3>
                        </div>
                        <ul class="space-y-3 mb-6 text-gray-700">
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-green-500 mr-1"></i>
                                    Shop from multiple stores
                                </span>
                            </li>
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-green-500 mr-1"></i>
                                    Track your orders
                                </span>
                            </li>
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-green-500 mr-1"></i>
                                    Save favorite products
                                </span>
                            </li>
                        </ul>
                        <Button @click="goToRegister" variant="primary" size="lg" full-width
                            class="bg-green-600 hover:bg-green-700 transition-colors cursor-pointer">
                            Register as Customer
                        </Button>
                    </div>

                    <!-- For Owners -->
                    <div class="bg-white rounded-lg shadow-lg p-8">
                        <div class="flex items-center mb-4">
                            <div class="p-4 bg-blue-100 rounded-full flex items-center justify-center mr-4">
                                <i class="fa-solid fa-store text-blue-600 text-2xl"></i>
                            </div>
                            <h3 class="text-2xl font-bold text-gray-900">For Store Owners</h3>
                        </div>
                        <ul class="space-y-3 mb-6 text-gray-700">
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-blue-500 mr-1"></i>
                                    Manage your store
                                </span>
                            </li>
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-blue-500 mr-1"></i>
                                    Add & manage products
                                </span>
                            </li>
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-blue-500 mr-1"></i>
                                    Track sales & analytics
                                </span>
                            </li>
                        </ul>
                        <Button @click="goToRegister" variant="primary" size="lg" full-width
                            class="bg-blue-600 hover:bg-blue-700 transition-colors cursor-pointer">
                            Register as Owner
                        </Button>
                    </div>

                </div>
            </div>
        </section>

        <!-- CTA Section -->
        <section class="py-20 bg-blue-600">
            <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
                <h2 class="text-4xl font-bold text-white mb-4">
                    Ready to Get Started?
                </h2>
                <p class="text-xl text-blue-100 mb-8 max-w-2xl mx-auto">
                    Utilize RetailHub for your business to simplify retail operations.
                </p>
                <div class="flex flex-col sm:flex-row gap-4 justify-center">
                    <Button @click="goToRegister" variant="outline" size="lg"
                        class="px-8 bg-white text-blue-600 border-white hover:bg-blue-700 hover:border-blue-700 hover:text-white cursor-pointer shadow-lg">
                        Get Started
                    </Button>
                    <Button @click="goToLogin" variant="outline" size="lg"
                        class="px-8 text-white border-white hover:bg-white hover:text-blue-700 cursor-pointer shadow-lg">
                        Sign In
                    </Button>
                </div>
            </div>
        </section>
    </DefaultLayout>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ROUTE_NAMES } from '../utils/constants';
import DefaultLayout from '../layouts/DefaultLayout.vue';
import Button from '../components/common/BaseButton.vue';

const router = useRouter();

const goToRegister = () => {
    router.push({ name: 'register' });
};

const goToLogin = () => {
    router.push({ name: ROUTE_NAMES.LOGIN });
};
</script>
<style scoped></style>