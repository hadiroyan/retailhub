<template>
    <DashboardLayout>
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">All Stores</h1>
                <p class="text-sm text-gray-500 mt-0.5">Manage all stores on the platform</p>
            </div>
            <div class="text-sm text-gray-500">
                Total: <span class="font-semibold text-gray-900">{{ totalStores }}</span> stores
            </div>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="flex items-center justify-center py-20">
            <i class="fas fa-spinner fa-spin text-blue-600 text-2xl"></i>
        </div>

        <!-- Error -->
        <div v-else-if="error" class="p-4 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
            {{ error }}
        </div>

        <!-- Empty -->
        <div v-else-if="stores.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
            <div class="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mb-4">
                <i class="fas fa-store text-gray-400 text-2xl"></i>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">No stores found</h3>
            <p class="text-sm text-gray-500">No stores have been created yet</p>
        </div>

        <!-- Store table -->
        <div v-else class="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b border-gray-200">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Store</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Owner</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Contact</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                    <tr v-for="store in stores" :key="store.id" class="hover:bg-gray-50 transition-colors">
                        <td class="px-4 py-3">
                            <p class="font-medium text-gray-900">{{ store.name }}</p>
                            <p class="text-xs text-gray-400 font-mono">{{ store.address }}</p>
                        </td>
                        <td class="px-4 py-3 text-gray-500">{{ store.owner?.fullName || '-' }}</td>
                        <td class="px-4 py-3">
                            <p class="text-gray-700">{{ store?.phone || '-' }}</p>
                            <p class="text-xs text-gray-400">{{ store.email }}</p>
                        </td>
                        <td class="px-4 py-3">
                            <StoreStatusBadge :status="store.status" />
                        </td>
                        <td class="px-4 py-3">
                            <div class="flex items-center gap-2">
                                <!-- Suspend -->
                                <button v-if="store.status !== STORE_STATUS.SUSPEND"
                                    @click="openStatusModal(store, STORE_STATUS.SUSPEND)"
                                    class="px-3 py-1.5 text-xs font-medium text-red-600 border border-red-400 rounded-lg hover:bg-red-50 transition-colors cursor-pointer">
                                    Suspend
                                </button>
                                <!-- Unsuspend / Reopen -->
                                <button v-if="store.status === STORE_STATUS.SUSPEND"
                                    @click="openStatusModal(store, STORE_STATUS.ACTIVE)"
                                    class="px-3 py-1.5 text-xs font-medium text-green-600 border border-green-400 rounded-lg hover:bg-green-50 transition-colors cursor-pointer">
                                    Unsuspend
                                </button>
                                <!-- Close -->
                                <button v-if="store.status === STORE_STATUS.ACTIVE"
                                    @click="openStatusModal(store, STORE_STATUS.CLOSED)"
                                    class="px-3 py-1.5 text-xs font-medium text-gray-600 border border-gray-400 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
                                    Close
                                </button>
                                <!-- Reopen -->
                                <button v-if="store.status === STORE_STATUS.CLOSED"
                                    @click="openStatusModal(store, STORE_STATUS.ACTIVE)"
                                    class="px-3 py-1.5 text-xs font-medium text-blue-600 border border-blue-400 rounded-lg hover:bg-blue-50 transition-colors cursor-pointer">
                                    Reopen
                                </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalStores > pageSize" class="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
            <p class="text-sm text-gray-500">Showing {{ stores.length }} of {{ totalStores }} stores</p>
            <div class="flex gap-2">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button @click="changePage(currentPage + 1)" :disabled="(currentPage + 1) * pageSize >= totalStores"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- Status change modal -->
        <StatusChangeModal v-model="showStatusModal" :target-status="targetStatus" :loading="statusLoading"
            @confirm="handleStatusChange" />

    </DashboardLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import StoreStatusBadge from '../../components/common/StoreStatusBadge.vue';
import StatusChangeModal from '../../components/modals/StatusChangeModal.vue';
import { useStoreStore } from '../../stores/storeStore';
import { STORE_STATUS } from '../../utils/constants';

const storeStore = useStoreStore();
const { stores, totalStores, currentPage, loading, error } = storeToRefs(storeStore);
const pageSize = ref(10);

const showStatusModal = ref(false);
const statusLoading = ref(false);
const targetStatus = ref('');
const selectedStore = ref(null);

const openStatusModal = (store, status) => {
    selectedStore.value = store;
    targetStatus.value = status;
    showStatusModal.value = true;
};

const handleStatusChange = async (status) => {
    statusLoading.value = true;
    try {
        await storeStore.updateStoreStatus(selectedStore.value.id, status);
        showStatusModal.value = false;
    } finally {
        statusLoading.value = false;
    }
};

const changePage = (page) => {
    storeStore.fetchStores(page, pageSize.value);
};

onMounted(async () => {
    await storeStore.fetchStores();
}); 
</script>

<style scoped></style>