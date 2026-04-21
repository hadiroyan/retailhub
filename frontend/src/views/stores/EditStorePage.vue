<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="mb-6">
            <button @click="router.push({ name: ROUTE_NAMES.OWNER_STORES })"
                class="flex items-center gap-2 text-sm text-gray-500 hover:text-blue-600 transition-colors mb-4">
                <i class="fas fa-arrow-left"></i>
                Back to My Stores
            </button>
            <h1 class="text-2xl font-bold text-gray-900">Edit Store</h1>
            <p class="text-sm text-gray-500 mt-0.5">Update your store information</p>
        </div>

        <!-- Loading store data -->
        <div v-if="loadingStore" class="flex items-center justify-center py-20">
            <i class="fas fa-spinner fa-spin text-blue-600 text-2xl"></i>
        </div>

        <!-- Store not found -->
        <div v-else-if="!currentStore" class="p-4 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
            Store not found.
        </div>

        <!-- Form card -->
        <div v-else class="bg-white rounded-xl border border-gray-200 p-6 max-w-2xl">
            <StoreForm :initial-data="currentStore" :loading="loading" :server-error="error" :is-edit="true"
                @submit="handleSubmit" @cancel="router.push({ name: ROUTE_NAMES.OWNER_STORES })" />
        </div>

    </DashboardLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import StoreForm from '../../components/forms/StoreForm.vue';
import { useStoreStore } from '../../stores/storeStore';
import { ROUTE_NAMES } from '../../utils/constants';

const router = useRouter();
const route = useRoute();
const storeStore = useStoreStore();
const { stores, loading, error } = storeToRefs(storeStore);

const loadingStore = ref(true);
const storeId = route.params.id;

// Find stores from the states that have been fetched in the OwnerDashboard
const currentStore = computed(() =>
    stores.value.find((s) => s.id === storeId) || null
);

onMounted(async () => {
    // If stores data hasn't been fetched yet (accessed directly via URL), fetch it first
    if (stores.value.length === 0) {
        await storeStore.fetchStores();
    }
    loadingStore.value = false;
});

const handleSubmit = async (data) => {
    try {
        await storeStore.updateStore(storeId, data);
        router.push({ name: ROUTE_NAMES.OWNER_STORES });
    } catch {
        // The error has been handled in storeStore
    }
};
</script>