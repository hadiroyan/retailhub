<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Orders</h1>
                <p class="text-sm text-gray-500 mt-0.5">Manage incoming orders</p>
            </div>
            <!-- Filter by status -->
            <select v-model="filterStatus" @change="fetchWithFilters"
                class="px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="">All Status</option>
                <option value="PENDING">PENDING</option>
                <option value="PROCESSING">PROCESSING</option>
                <option value="SHIPPED">SHIPPED</option>
                <option value="DELIVERED">DELIVERED</option>
                <option value="CANCELLED">CANCELLED</option>
            </select>
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
        <div v-else-if="storeOrders.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
            <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                <i class="fas fa-shopping-bag text-blue-400 text-2xl"></i>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">No orders yet</h3>
            <p class="text-sm text-gray-500">Orders will appear here when customers place them</p>
        </div>

        <!-- Order table -->
        <div v-else class="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b border-gray-200">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Order</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Customer</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Items</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Total</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                        <th v-if="canUpdateStatus" class="text-left px-4 py-3 font-medium text-gray-600">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                    <tr v-for="order in storeOrders" :key="order.id" class="hover:bg-gray-50 transition-colors">
                        <td class="px-4 py-3">
                            <p class="font-mono text-xs font-medium text-gray-900">{{ order.orderNumber }}</p>
                            <p class="text-xs text-gray-400">{{ formatDate(order.createdAt) }}</p>
                        </td>
                        <td class="px-4 py-3">
                            <p class="text-sm font-medium text-gray-900">{{ order.recipientName }}</p>
                            <p class="text-xs text-gray-400">{{ order.phone }}</p>
                        </td>
                        <td class="px-4 py-3">
                            <p class="text-sm text-gray-700">{{ order.items?.length }} item(s)</p>
                            <p class="text-xs text-gray-400 truncate max-w-32">
                                {{order.items?.map(i => i.name).join(', ')}}
                            </p>
                        </td>
                        <td class="px-4 py-3 font-medium text-gray-900">
                            {{ formatCurrency(order.totalAmount) }}
                        </td>
                        <td class="px-4 py-3">
                            <span class="text-xs px-2 py-0.5 rounded-full font-medium"
                                :class="statusBadgeClass(order.status)">
                                {{ order.status }}
                            </span>
                        </td>
                        <td v-if="canUpdateStatus" class="px-4 py-3">
                            <div class="flex items-center gap-1">
                                <button @click="openDetail(order)"
                                    class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                    title="View detail">
                                    <i class="fas fa-eye text-xs"></i>
                                </button>
                                <button v-if="order.status !== 'DELIVERED' && order.status !== 'CANCELLED'"
                                    @click="openStatusModal(order)"
                                    class="p-1.5 text-gray-400 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                                    title="Update status">
                                    <i class="fas fa-edit text-xs"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalStoreOrders > pageSize"
            class="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
            <p class="text-sm text-gray-500">Showing {{ storeOrders.length }} of {{ totalStoreOrders }} orders</p>
            <div class="flex gap-2">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button @click="changePage(currentPage + 1)"
                    :disabled="(currentPage + 1) * pageSize >= totalStoreOrders"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- Order detail modal -->
        <Teleport to="body">
            <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                @click.self="showDetailModal = false">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
                    <h3 class="text-lg font-semibold text-gray-900 mb-1">Order Detail</h3>
                    <p class="text-xs text-gray-400 font-mono mb-4">{{ selectedOrder?.orderNumber }}</p>

                    <div class="space-y-3 text-sm mb-4">
                        <div class="flex justify-between">
                            <span class="text-gray-500">Status</span>
                            <span class="px-2 py-0.5 rounded-full text-xs font-medium"
                                :class="statusBadgeClass(selectedOrder?.status)">
                                {{ selectedOrder?.status }}
                            </span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-gray-500">Recipient</span>
                            <span class="font-medium text-gray-900">{{ selectedOrder?.recipientName }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-gray-500">Phone</span>
                            <span class="font-medium text-gray-900">{{ selectedOrder?.phone }}</span>
                        </div>
                        <div class="flex flex-col gap-1">
                            <span class="text-gray-500">Shipping Address</span>
                            <span class="font-medium text-gray-900">{{ selectedOrder?.shippingAddress }}</span>
                        </div>
                        <div v-if="selectedOrder?.trackingNumber" class="flex justify-between">
                            <span class="text-gray-500">Tracking Number</span>
                            <span class="font-medium text-blue-600">{{ selectedOrder?.trackingNumber }}</span>
                        </div>
                        <div v-if="selectedOrder?.notes" class="flex flex-col gap-1">
                            <span class="text-gray-500">Notes</span>
                            <span class="font-medium text-gray-900">{{ selectedOrder?.notes }}</span>
                        </div>
                    </div>

                    <!-- Items -->
                    <div class="border-t border-gray-200 pt-4 mb-4">
                        <p class="text-sm font-medium text-gray-700 mb-3">Items</p>
                        <div class="space-y-2">
                            <div v-for="item in selectedOrder?.items" :key="item.id" class="flex items-center gap-3">
                                <div
                                    class="w-10 h-10 bg-gray-100 rounded-lg overflow-hidden flex items-center justify-center shrink-0">
                                    <img v-if="item.imageUrl" :src="getImageUrl(item.imageUrl)" :alt="item.name"
                                        class="w-full h-full object-cover" />
                                    <i v-else class="fas fa-box text-gray-300 text-sm"></i>
                                </div>
                                <div class="flex-1 min-w-0">
                                    <p class="text-sm font-medium text-gray-900 truncate">{{ item.name }}</p>
                                    <p class="text-xs text-gray-400 font-mono">{{ item.sku }}</p>
                                    <p class="text-xs text-gray-400">{{ item.quantity }} x {{
                                        formatCurrency(item.unitPrice) }}</p>
                                </div>
                                <p class="text-sm font-semibold text-gray-900">{{ formatCurrency(item.subtotal) }}</p>
                            </div>
                        </div>
                    </div>

                    <div class="flex justify-between border-t border-gray-200 pt-3 mb-5">
                        <span class="font-semibold text-gray-900">Total</span>
                        <span class="font-bold text-blue-600">{{ formatCurrency(selectedOrder?.totalAmount) }}</span>
                    </div>

                    <button @click="showDetailModal = false"
                        class="w-full px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                        Close
                    </button>
                </div>
            </div>
        </Teleport>

        <!-- Update status modal -->
        <Teleport to="body">
            <div v-if="showStatusModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                @click.self="showStatusModal = false">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-sm p-6">
                    <h3 class="text-lg font-semibold text-gray-900 mb-1">Update Status</h3>
                    <p class="text-xs text-gray-400 font-mono mb-4">{{ selectedOrder?.orderNumber }}</p>

                    <div class="space-y-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">New Status</label>
                            <select v-model="newStatus"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                                <option v-for="status in availableStatuses" :key="status" :value="status">
                                    {{ status }}
                                </option>
                            </select>
                        </div>

                        <!-- Tracking number — only for SHIPPED -->
                        <div v-if="newStatus === 'SHIPPED'">
                            <label class="block text-sm font-medium text-gray-700 mb-1">Tracking Number</label>
                            <input v-model="trackingNumber" type="text" placeholder="e.g. JNE-12345"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>

                        <div v-if="statusError"
                            class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                            {{ statusError }}
                        </div>
                    </div>

                    <div class="flex gap-3 mt-5">
                        <button @click="showStatusModal = false"
                            class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                            Cancel
                        </button>
                        <button @click="handleUpdateStatus" :disabled="statusLoading"
                            class="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                            <span v-if="statusLoading"><i class="fas fa-spinner animate-spin mr-1"></i></span>
                            <span v-else>Update</span>
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

    </DashboardLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import { useOrderStore } from '../../stores/orderStore';
import { useAuthStore } from '../../stores/auth';
import { ROLES } from '../../utils/constants';
import { formatCurrency, formatDate, getImageUrl } from '../../utils/helper';

const route = useRoute();
const orderStore = useOrderStore();
const authStore = useAuthStore();

const { storeOrders, totalStoreOrders, loading, error } = storeToRefs(orderStore);
const { userRole } = storeToRefs(authStore);

const storeId = computed(() => route.params.storeId || orderStore.activeStore?.id);
const pageSize = ref(10);
const currentPage = ref(0);
const filterStatus = ref('');

const canUpdateStatus = computed(() =>
    [ROLES.OWNER, ROLES.ADMIN, ROLES.MANAGER].includes(userRole.value)
);

const statusBadgeClass = (status) => {
    const map = {
        PENDING: 'bg-yellow-100 text-yellow-700',
        PROCESSING: 'bg-blue-100 text-blue-700',
        SHIPPED: 'bg-indigo-100 text-indigo-700',
        DELIVERED: 'bg-green-100 text-green-700',
        CANCELLED: 'bg-red-100 text-red-700',
    };
    return map[status] || 'bg-gray-100 text-gray-600';
};

const fetchWithFilters = () => {
    currentPage.value = 0;
    orderStore.fetchStoreOrders(storeId.value, {
        status: filterStatus.value || undefined,
        page: 0,
        size: pageSize.value,
    });
};

const changePage = (page) => {
    currentPage.value = page;
    orderStore.fetchStoreOrders(storeId.value, {
        status: filterStatus.value || undefined,
        page,
        size: pageSize.value,
    });
};

// Detail modal
const showDetailModal = ref(false);
const selectedOrder = ref(null);

const openDetail = (order) => {
    selectedOrder.value = order;
    showDetailModal.value = true;
};

// Status modal
const showStatusModal = ref(false);
const statusLoading = ref(false);
const statusError = ref(null);
const newStatus = ref('');
const trackingNumber = ref('');

const availableStatuses = computed(() => {
    if (!selectedOrder.value) return [];
    const transitions = {
        PENDING: ['PROCESSING', 'CANCELLED'],
        PROCESSING: ['SHIPPED', 'CANCELLED'],
        SHIPPED: ['DELIVERED'],
    };
    return transitions[selectedOrder.value.status] || [];
});

const openStatusModal = (order) => {
    selectedOrder.value = order;
    newStatus.value = availableStatuses.value[0] || '';
    trackingNumber.value = '';
    statusError.value = null;
    showStatusModal.value = true;
};

const handleUpdateStatus = async () => {
    statusLoading.value = true;
    statusError.value = null;
    try {
        await orderStore.updateOrderStatus(storeId.value, selectedOrder.value.id, {
            status: newStatus.value,
            trackingNumber: newStatus.value === 'SHIPPED' ? trackingNumber.value : undefined,
        });
        showStatusModal.value = false;
        fetchWithFilters();
    } catch (err) {
        statusError.value = err.response?.data?.message || 'Failed to update status';
    } finally {
        statusLoading.value = false;
    }
};

onMounted(() => {
    fetchWithFilters();
});
</script>