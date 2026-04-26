<template>
    <CustomerLayout>
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

            <!-- Header -->
            <div class="mb-6">
                <h1 class="text-2xl font-bold text-gray-900">My Orders</h1>
                <p class="text-sm text-gray-500 mt-0.5">Your order history</p>
            </div>

            <!-- Coming soon placeholder -->
            <div
                class="bg-white rounded-xl border border-gray-200 p-12 flex flex-col items-center justify-center text-center">
                <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                    <i class="fas fa-box text-blue-400 text-2xl"></i>
                </div>
                <h3 class="font-semibold text-gray-900 mb-1">No orders yet</h3>
                <p class="text-sm text-gray-500 mb-2">Your orders will appear here once backend is ready</p>
                <span class="text-xs px-2 py-0.5 bg-gray-100 text-gray-400 rounded-full">Coming soon</span>
                <button @click="router.push({ name: ROUTE_NAMES.EXPLORE })"
                    class="mt-6 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                    Start Shopping
                </button>
            </div>

        </div>
    </CustomerLayout>
</template>

<script setup>
import { useRouter } from 'vue-router';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import { ROUTE_NAMES } from '../../utils/constants';

const router = useRouter();
</script>
