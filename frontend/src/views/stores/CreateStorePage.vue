<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="mb-6">
            <button @click="router.push({ name: ROUTE_NAMES.OWNER_STORES })"
                class="flex items-center gap-2 text-sm text-gray-500 hover:text-blue-600 transition-colors mb-4">
                <i class="fas fa-arrow-left"></i>
                Back to My Stores
            </button>
            <h1 class="text-2xl font-bold text-gray-900">Create Store</h1>
            <p class="text-sm text-gray-500 mt-0.5">Fill in the details to create your new store</p>
        </div>

        <!-- Form card -->
        <div class="bg-white rounded-xl border border-gray-200 p-6 max-w-2xl">
            <StoreForm :loading="loading" :server-error="error" @submit="handleSubmit"
                @cancel="router.push({ name: ROUTE_NAMES.OWNER_STORES })" />
        </div>

    </DashboardLayout>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import StoreForm from '../../components/forms/StoreForm.vue';
import { useStoreStore } from '../../stores/storeStore';
import { ROUTE_NAMES } from '../../utils/constants';

const router = useRouter();
const storeStore = useStoreStore();
const { loading, error } = storeToRefs(storeStore);

const handleSubmit = async (data) => {
    try {
        await storeStore.createStore(data);
        router.push({ name: ROUTE_NAMES.OWNER_STORES });
    } catch {
        //  The error has been handled in storeStore
    }
};
</script>