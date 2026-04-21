<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">My Stores</h1>
                <p class="text-sm text-gray-500 mt-0.5">Manage your stores</p>
            </div>
            <button @click="router.push({ name: ROUTE_NAMES.OWNER_STORE_CREATE })"
                class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                <i class="fas fa-plus"></i>
                <span>Create Store</span>
            </button>
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
            <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                <i class="fas fa-store text-blue-400 text-2xl"></i>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">No stores yet</h3>
            <p class="text-sm text-gray-500 mb-4">Create your first store to get started</p>
            <button @click="router.push({ name: ROUTE_NAMES.OWNER_STORE_CREATE })"
                class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                Create Store
            </button>
        </div>

        <!-- Store list -->
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <StoreCard v-for="store in stores" :key="store.id" :store="store">
                <template #actions>
                    <button @click="goToStore(store)"
                        class="px-3 py-1.5 text-xs font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded-lg transition-colors">
                        Manage
                    </button>
                    <button @click="openEdit(store)"
                        class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-gray-100 rounded-lg transition-colors">
                        <i class="fas fa-pen text-xs"></i>
                    </button>
                    <button @click="openDelete(store)"
                        class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                        <i class="fas fa-trash text-xs"></i>
                    </button>
                </template>
            </StoreCard>
        </div>

        <!-- Pagination -->
        <div v-if="totalStores > pageSize" class="flex items-center justify-between mt-6 pt-4 border-t border-gray-200">
            <p class="text-sm text-gray-500">
                Showing {{ stores.length }} of {{ totalStores }} stores
            </p>
            <div class="flex gap-2">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button @click="changePage(currentPage + 1)" :disabled="(currentPage + 1) * pageSize >= totalStores"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- Delete modal -->
        <DeleteConfirmModal v-model="showDeleteModal" :title="`Delete ${selectedStore?.name}?`"
            description="This store and all its data will be permanently deleted. This action cannot be undone."
            :loading="deleteLoading" @confirm="handleDelete" />

    </DashboardLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import StoreCard from '../../components/common/StoreCard.vue';
import DeleteConfirmModal from '../../components/modals/DeleteConfirmModal.vue';
import { useStoreStore } from '../../stores/storeStore';
import { ROUTE_NAMES } from '../../utils/constants';

const router = useRouter();
const storeStore = useStoreStore();
const { stores, totalStores, currentPage, pageSize, loading, error } = storeToRefs(storeStore);

const showDeleteModal = ref(false);
const selectedStore = ref(null);
const deleteLoading = ref(false);

onMounted(() => {
    storeStore.fetchStores();
});

const goToStore = (store) => {
    storeStore.setActiveStore(store);
    router.push({ name: ROUTE_NAMES.STORE_DASHBOARD, params: { storeId: store.id } });
};

const openEdit = (store) => {
    router.push({ name: ROUTE_NAMES.OWNER_STORE_EDIT, params: { id: store.id } });
};

const openDelete = (store) => {
    selectedStore.value = store;
    showDeleteModal.value = true;
};

const handleDelete = async () => {
    if (!selectedStore.value) return;
    deleteLoading.value = true;
    try {
        await storeStore.deleteStore(selectedStore.value.id);
        showDeleteModal.value = false;
        selectedStore.value = null;
    } finally {
        deleteLoading.value = false;
    }
};

const changePage = (page) => {
    storeStore.fetchStores(page);
};
</script>