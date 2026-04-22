<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="mb-6">
            <h1 class="text-2xl font-bold text-gray-900">Store Settings</h1>
            <p class="text-sm text-gray-500 mt-0.5">Update your store information</p>
        </div>

        <!-- Loading -->
        <div v-if="!activeStore" class="flex items-center justify-center py-20">
            <i class="fas fa-spinner fa-spin text-blue-600 text-2xl"></i>
        </div>

        <div v-else class="max-w-2xl space-y-6">

            <!-- Store form -->
            <div class="bg-white rounded-xl border border-gray-200 p-6">
                <h2 class="text-base font-semibold text-gray-900 mb-4">General Info</h2>
                <StoreForm :initial-data="activeStore" :loading="loading" :server-error="error" :is-edit="true"
                    @submit="handleUpdate"
                    @cancel="router.push({ name: ROUTE_NAMES.STORE_DASHBOARD, params: { storeId: activeStore.id } })" />
            </div>

            <!-- Advanced Settings — OWNER only -->
            <div class="bg-white rounded-xl border border-red-200 p-6">
                <h2 class="text-base font-semibold text-red-600 mb-1">Advanced Settings</h2>
                <p class="text-sm text-gray-500 mb-4">
                    These actions are irreversible. Please proceed with caution.
                </p>
                <!-- Current status -->
                <div class="flex items-center mb-4">
                    <div>
                        <p class="text-sm font-medium text-gray-700 mr-2">Store Status: </p>
                    </div>
                    <StoreStatusBadge :status="activeStore.status" />
                </div>
                <!-- Actions based on status -->
                <div class="flex flex-col sm:flex-row gap-3">
                    <!-- If CLOSED -> display Reopen -->
                    <button v-if="activeStore.status === STORE_STATUS.CLOSED"
                        @click="openStatusModal(STORE_STATUS.ACTIVE)"
                        class="px-4 py-2 text-sm font-medium text-green-600 border border-green-500 rounded-lg hover:bg-green-50 transition-colors cursor-pointer">
                        <i class="fas fa-door-open mr-2"></i>Reopen Store
                    </button>

                    <!-- If ACTIVE -> display Close -->
                    <button v-if="activeStore.status === STORE_STATUS.ACTIVE"
                        @click="openStatusModal(STORE_STATUS.CLOSED)"
                        class="px-4 py-2 text-sm font-medium text-gray-700 border border-gray-500 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
                        <i class="fas fa-door-closed mr-2"></i>Close Store
                    </button>

                    <!-- Delete always displayed -->
                    <button @click="openDeleteModal"
                        class="px-4 py-2 text-sm font-medium text-red-600 border border-red-500 rounded-lg hover:bg-red-50 transition-colors cursor-pointer">
                        <i class="fas fa-trash mr-2"></i>Delete Store
                    </button>
                </div>
            </div>

        </div>

        <!-- Status modal -->
        <StatusChangeModal v-model="showStatusModal" :target-status="targetStatus" :loading="statusLoading"
            @confirm="handleStatusChange" />

        <!-- Delete modal -->
        <DeleteConfirmModal v-model="showDeleteModal" :title="`Delete ${activeStore?.name}?`"
            description="This store and all its data will be permanently deleted." :loading="deleteLoading"
            @confirm="handleDelete" />

    </DashboardLayout>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import StoreForm from '../../components/forms/StoreForm.vue';
import StatusChangeModal from '../../components/modals/StatusChangeModal.vue';
import DeleteConfirmModal from '../../components/modals/DeleteConfirmModal.vue';
import StoreStatusBadge from '../../components/common/StoreStatusBadge.vue';
import { useStoreStore } from '../../stores/storeStore';
import { ROUTE_NAMES, STORE_STATUS } from '../../utils/constants';

const router = useRouter();
const storeStore = useStoreStore();
const { activeStore, loading, error } = storeToRefs(storeStore);

const showStatusModal = ref(false);
const showDeleteModal = ref(false);
const targetStatus = ref('');
const statusLoading = ref(false);
const deleteLoading = ref(false);

const handleUpdate = async (data) => {
    try {
        const updated = await storeStore.updateStore(activeStore.value.id, data);
        storeStore.setActiveStore(updated);
    } catch {
        // Error handled in storeStore
    }
};

const openStatusModal = (status) => {
    targetStatus.value = status;
    showStatusModal.value = true;
};

const handleStatusChange = async (status) => {
    statusLoading.value = true;
    try {
        const updated = await storeStore.updateStoreStatus(activeStore.value.id, status);
        storeStore.setActiveStore(updated);
        showStatusModal.value = false;
    } finally {
        statusLoading.value = false;
    }
};

const openDeleteModal = () => {
    showDeleteModal.value = true;
};

const handleDelete = async () => {
    deleteLoading.value = true;
    try {
        await storeStore.deleteStore(activeStore.value.id);
        storeStore.setActiveStore(null);
        showDeleteModal.value = false;
        router.push({ name: ROUTE_NAMES.OWNER_STORES });
    } finally {
        deleteLoading.value = false;
    }
};
</script>