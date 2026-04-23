<template>
    <DashboardLayout>
        <!-- Header -->
        <div class="mb-6">
            <h1 class="text-2xl font-bold text-gray-900">Dashboard</h1>
            <p class="text-sm text-gray-500 mt-0.5">Overview of {{ activeStore?.name }}</p>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="flex items-center justify-center py-20">
            <i class="fas fa-spinner fa-spin text-blue-600 text-2xl"></i>
        </div>

        <template v-else>

            <!-- Stats grid -->
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                <div v-for="stat in stats" :key="stat.label" class="bg-white rounded-xl border p-5"
                    :class="stat.highlight ? 'border-red-200' : 'border-gray-200'">
                    <div class="flex items-center justify-between mb-3">
                        <span class="text-sm text-gray-500">{{ stat.label }}</span>
                        <div class="w-9 h-9 rounded-lg flex items-center justify-center" :class="stat.iconBg">
                            <i class="fas text-sm" :class="[stat.icon, stat.iconColor]"></i>
                        </div>
                    </div>
                    <p class="text-3xl font-bold" :class="stat.valueColor">{{ stat.value }}</p>
                </div>
            </div>

            <!-- Store info -->
            <div class="bg-white rounded-xl border border-gray-200 p-5">
                <h2 class="text-base font-semibold text-gray-900 mb-4">Store Info</h2>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
                    <div>
                        <p class="text-gray-400 mb-0.5">Store Name</p>
                        <p class="font-medium text-gray-800">{{ activeStore?.name }}</p>
                    </div>
                    <div>
                        <p class="text-gray-400 mb-0.5">Status</p>
                        <StoreStatusBadge v-if="activeStore?.status" :status="activeStore.status" />
                    </div>
                    <div>
                        <p class="text-gray-400 mb-0.5">Address</p>
                        <p class="font-medium text-gray-800">{{ activeStore?.address || '-' }}</p>
                    </div>
                    <div>
                        <p class="text-gray-400 mb-0.5">Phone</p>
                        <p class="font-medium text-gray-800">{{ activeStore?.phone || '-' }}</p>
                    </div>
                    <div>
                        <p class="text-gray-400 mb-0.5">Email</p>
                        <p class="font-medium text-gray-800">{{ activeStore?.email || '-' }}</p>
                    </div>
                    <div>
                        <p class="text-gray-400 mb-0.5">Slug</p>
                        <p class="font-medium text-gray-800 font-mono text-xs">{{ activeStore?.slug }}</p>
                    </div>
                </div>
            </div>

        </template>

    </DashboardLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import StoreStatusBadge from '../../components/common/StoreStatusBadge.vue';
import { useStoreStore } from '../../stores/storeStore';

const route = useRoute();
const storeStore = useStoreStore();
const {
    activeStore,
    loading,
    totalProducts,
    totalCategories,
    totalEmployees,
    lowStockProducts,
} = storeToRefs(storeStore);

const storeId = computed(() => route.params.storeId);

const stats = computed(() => [
    {
        label: 'Total Products',
        value: totalProducts.value,
        icon: 'fa-box',
        iconBg: 'bg-blue-50',
        iconColor: 'text-blue-500',
        valueColor: 'text-blue-700',
        highlight: false,
    },
    {
        label: 'Total Categories',
        value: totalCategories.value,
        icon: 'fa-folder',
        iconBg: 'bg-indigo-50',
        iconColor: 'text-indigo-500',
        valueColor: 'text-indigo-700',
        highlight: false,
    },
    {
        label: 'Total Employees',
        value: totalEmployees.value,
        icon: 'fa-users',
        iconBg: 'bg-green-50',
        iconColor: 'text-green-500',
        valueColor: 'text-green-700',
        highlight: false,
    },
    {
        label: 'Low Stock',
        value: lowStockProducts.value,
        icon: 'fa-exclamation-triangle',
        iconBg: 'bg-red-50',
        iconColor: 'text-red-500',
        valueColor: 'text-red-700',
        highlight: false,
    },
]);

onMounted(async () => {
    if (!activeStore.value) return;
    const id = storeId.value;
    await Promise.all([
        storeStore.fetchStoreBySlug(activeStore.value?.slug),
        storeStore.fetchInternalProducts(id),
        storeStore.fetchCategories(id),
        storeStore.fetchEmployees(id),
    ]);
});
</script>