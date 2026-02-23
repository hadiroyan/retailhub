<template>
    <ChoiceLayout>
        <div class="space-y-4">
            <!-- Title -->
            <div class="text-center">
                <h2 class="text-3xl font-bold text-gray-900">Join RetailHub</h2>
                <p class="mt-2 text-sm text-gray-600">
                    Choose your account type to get started
                </p>
            </div>

            <!-- Role Selection Cards -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
                <!-- Customer Card -->
                <Card padding="lg" shadow="lg"
                    class="cursor-pointer transition-all duration-200 hover:shadow-xl hover:scale-105 border-2 border-transparent hover:border-green-500"
                    @click="selectCustomer">
                    <div class="text-center space-y-4">
                        <!-- Icon -->
                        <div class="flex justify-center items-center">
                            <div class="p-4 bg-green-100 rounded-full">
                                <i class="fa-solid fa-cart-shopping text-green-600 text-5xl"></i>
                            </div>
                        </div>

                        <!-- Title -->
                        <h3 class="text-xl font-bold text-gray-900">Customer</h3>

                        <!-- Description -->
                        <p class="text-sm text-gray-600">
                            Browse products, make purchases, and manage your orders
                        </p>

                        <!-- Features -->
                        <ul class="text-left text-sm text-gray-600 space-y-2">
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-green-500 mr-2"></i>
                                    Shop from multiple stores
                                </span>
                            </li>
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-green-500 mr-2"></i>
                                    Track your orders
                                </span>
                            </li>
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-green-500 mr-2"></i>
                                    Save favorite products
                                </span>
                            </li>
                        </ul>

                        <!-- Button -->
                        <div class="pt-4">
                            <button @click.stop="selectCustomer"
                                class="w-full bg-green-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-green-700 transition-colors">
                                Register as Customer
                            </button>
                        </div>
                    </div>
                </Card>

                <!-- Owner Card -->
                <Card padding="lg" shadow="lg"
                    class="cursor-pointer transition-all duration-200 hover:shadow-xl hover:scale-105 border-2 border-transparent hover:border-blue-500"
                    @click="selectOwner">
                    <div class="text-center space-y-4">
                        <!-- Icon -->
                        <div class="flex justify-center items-center">
                            <div class="p-4 bg-blue-100 rounded-full">
                                <i class="fa-solid fa-store text-blue-600 text-5xl"></i>
                            </div>
                        </div>

                        <!-- Title -->
                        <h3 class="text-xl font-bold text-gray-900">Store Owner</h3>

                        <!-- Description -->
                        <p class="text-sm text-gray-600">
                            Create and manage your store, products, and sales
                        </p>

                        <!-- Features -->
                        <ul class="text-left text-sm text-gray-600 space-y-2">
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-blue-500 mr-2"></i>
                                    Manage your store
                                </span>
                            </li>
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-blue-500 mr-2"></i>
                                    Add & manage products
                                </span>
                            </li>
                            <li class="flex items-start">
                                <span>
                                    <i class="fa-solid fa-circle-check text-blue-500 mr-2"></i>
                                    Track sales & analytics
                                </span>
                            </li>
                        </ul>

                        <!-- Button -->
                        <div class="pt-4">
                            <button @click.stop="selectOwner"
                                class="w-full bg-blue-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-blue-700 transition-colors">
                                Register as Owner
                            </button>
                        </div>
                    </div>
                </Card>
            </div>

            <!-- Divider -->
            <div class="relative mt-8">
                <div class="absolute inset-0 flex items-center">
                    <div class="w-full border-t border-gray-300"></div>
                </div>
                <div class="relative flex justify-center text-sm">
                    <span class="px-2 bg-indigo-50 text-gray-500">Already have an account?</span>
                </div>
            </div>

            <!-- Login Link -->
            <button @click="goToLogin"
                class="w-full text-center text-blue-600 hover:text-blue-700 font-medium hover:underline cursor-pointer">
                Sign in to your account
            </button>
        </div>
    </ChoiceLayout>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ROUTE_NAMES } from '../../utils/constants';
import Card from '../../components/common/BaseCard.vue';
import ChoiceLayout from '../../layouts/ChoiceLayout.vue';

const router = useRouter();

const selectCustomer = () => {
    router.push({ name: ROUTE_NAMES.REGISTER_CUSTOMER });
};

const selectOwner = () => {
    router.push({ name: ROUTE_NAMES.REGISTER_OWNER });
};

const goToLogin = () => {
    router.push({ name: ROUTE_NAMES.LOGIN });
};
</script>

<style scoped></style>