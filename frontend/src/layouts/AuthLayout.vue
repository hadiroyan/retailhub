<script setup>
</script>

<template>
    <div class="min-h-screen bg-linear-to-br from-blue-50 to-indigo-100 flex items-center justify-center px-4 py-8">
        <div class="w-full max-w-md">
            <!-- Logo/Brand Section -->
            <div class="text-center mb-8">
                <h1 class="text-4xl font-bold text-gray-900 mb-2">
                    RetailHub
                </h1>
                <p class="text-gray-600">Multi-Store Retail Management Platform</p>
            </div>

            <!-- Main Content (Card wrapper) -->
            <div class="bg-white rounded-lg shadow-xl p-8">
                <slot></slot>
            </div>

            <!-- Footer Links -->
            <div class="mt-6 text-center text-sm text-gray-600">
                <p>&copy; 2026 RetailHub. All rights reserved.</p>
            </div>
        </div>
    </div>
</template>

<style scoped></style>