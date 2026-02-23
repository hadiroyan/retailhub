<template>
    <div class="min-h-screen bg-indigo-50 flex items-center justify-center px-4 py-8">
        <div class="w-full max-w-4xl"> 

            <!-- Content (NO CARD WRAPPER) -->
            <slot></slot>

            <!-- Footer -->
            <div class="mt-6 text-center text-sm text-gray-600">
                <p>&copy; 2026 RetailHub. All rights reserved.</p>
            </div>
        </div>
    </div>
</template>

<script setup></script>

<style scoped></style>