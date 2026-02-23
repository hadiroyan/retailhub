<template>
    <div class="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div class="max-w-lg w-full text-center">
            <h1 class="text-9xl font-bold text-blue-600 mb-4">
                404!
            </h1>

            <h2 class="text-3xl font-bold text-gray-900 mb-4">Page Not Found</h2>

            <p class="text-gray-600 mb-8">
                The page you're looking for doesn't exist. It might have been moved or deleted.
            </p>

            <div class="flex gap-4 justify-center">
                <Button @click="goBack" variant="outline">
                    Go Back
                </Button>
                <Button @click="goHome" variant="primary">
                    Go Home
                </Button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { ROUTE_NAMES } from '../utils/constants';
import Button from '../components/common/BaseButton.vue';

const router = useRouter();

const goHome = () => {
    router.push({ name: ROUTE_NAMES.HOME });
};

const goBack = () => {
    router.back();
};
</script>

<style scoped></style>