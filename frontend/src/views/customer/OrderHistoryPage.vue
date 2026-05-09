<template>
    <CustomerLayout>
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

            <!-- Header -->
            <div class="mb-6">
                <h1 class="text-2xl font-bold text-gray-900">My Orders</h1>
                <p class="text-sm text-gray-500 mt-0.5">Track and manage your orders</p>
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
            <div v-else-if="orders.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
                <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                    <i class="fas fa-box text-blue-400 text-2xl"></i>
                </div>
                <h3 class="font-semibold text-gray-900 mb-1">No orders yet</h3>
                <p class="text-sm text-gray-500 mb-4">Start shopping to see your orders here</p>
                <button @click="router.push({ name: ROUTE_NAMES.EXPLORE })"
                    class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                    Start Shopping
                </button>
            </div>

            <!-- Order list -->
            <div v-else class="space-y-4">
                <div v-for="order in orders" :key="order.id"
                    class="bg-white rounded-xl border border-gray-200 overflow-hidden">

                    <!-- Order header -->
                    <div class="flex items-center justify-between px-4 py-3 bg-gray-50 border-b border-gray-200">
                        <div class="flex items-center gap-3">
                            <span class="text-sm font-mono font-medium text-gray-900">{{ order.orderNumber }}</span>
                            <span class="text-xs px-2 py-0.5 rounded-full font-medium"
                                :class="statusBadgeClass(order.status)">
                                {{ order.status }}
                            </span>
                        </div>
                        <span class="text-xs text-gray-400">{{ formatDate(order.createdAt) }}</span>
                    </div>

                    <!-- Store info -->
                    <div class="flex items-center gap-2 px-4 py-2 border-b border-gray-100">
                        <i class="fas fa-store text-gray-400 text-xs"></i>
                        <span class="text-sm text-gray-600">{{ order.store?.name }}</span>
                    </div>

                    <!-- Items preview -->
                    <div class="px-4 py-3 space-y-2">
                        <div v-for="item in order.items" :key="item.id" class="flex items-center gap-3">
                            <div
                                class="w-10 h-10 bg-gray-100 rounded-lg overflow-hidden flex items-center justify-center shrink-0">
                                <img v-if="item.imageUrl" :src="getImageUrl(item.imageUrl)" :alt="item.name"
                                    class="w-full h-full object-cover" />
                                <i v-else class="fas fa-box text-gray-300 text-sm"></i>
                            </div>
                            <div class="flex-1 min-w-0">
                                <p class="text-sm font-medium text-gray-900 truncate">{{ item.name }}</p>
                                <p class="text-xs text-gray-400">{{ item.quantity }} x {{ formatCurrency(item.unitPrice)
                                }}</p>
                            </div>
                            <p class="text-sm font-medium text-gray-900 shrink-0">
                                {{ formatCurrency(item.subtotal) }}
                            </p>
                        </div>
                    </div>

                    <!-- Order footer -->
                    <div class="flex items-center justify-between px-4 py-3 bg-gray-50 border-t border-gray-200">
                        <div class="text-sm">
                            <span class="text-gray-500">Total: </span>
                            <span class="font-semibold text-gray-900">{{ formatCurrency(order.totalAmount) }}</span>
                        </div>
                        <div class="flex items-center gap-2">
                            <!-- Tracking number -->
                            <span v-if="order.trackingNumber" class="text-xs text-gray-500">
                                <i class="fas fa-truck mr-1"></i>{{ order.trackingNumber }}
                            </span>
                            <!-- Cancel button -->
                            <button v-if="order.status === 'PENDING' || order.status === 'PROCESSING'"
                                @click="openCancelModal(order)"
                                class="px-3 py-1.5 text-xs font-medium text-red-600 border border-red-200 hover:bg-red-50 rounded-lg transition-colors">
                                Cancel
                            </button>
                            <!-- Detail button -->
                            <button @click="openDetail(order)"
                                class="px-3 py-1.5 text-xs font-medium text-blue-600 border border-blue-200 hover:bg-blue-50 rounded-lg transition-colors">
                                Detail
                            </button>
                        </div>
                    </div>

                </div>
            </div>

            <!-- Pagination -->
            <div v-if="totalOrders > pageSize"
                class="flex items-center justify-between mt-6 pt-4 border-t border-gray-200">
                <p class="text-sm text-gray-500">Showing {{ orders.length }} of {{ totalOrders }} orders</p>
                <div class="flex gap-2">
                    <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                        class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                        <i class="fas fa-chevron-left"></i>
                    </button>
                    <button @click="changePage(currentPage + 1)" :disabled="(currentPage + 1) * pageSize >= totalOrders"
                        class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                        <i class="fas fa-chevron-right"></i>
                    </button>
                </div>
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
                            <span class="text-gray-500">Store</span>
                            <span class="font-medium text-gray-900">{{ selectedOrder?.store?.name }}</span>
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
                                    <p class="text-xs text-gray-400">{{ item.quantity }} x {{
                                        formatCurrency(item.unitPrice) }}</p>
                                </div>
                                <p class="text-sm font-semibold text-gray-900">{{ formatCurrency(item.subtotal) }}</p>
                            </div>
                        </div>
                    </div>

                    <!-- Total -->
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

        <!-- Cancel confirm modal -->
        <DeleteConfirmModal v-model="showCancelModal" :title="`Cancel Order ${selectedOrder?.orderNumber}?`"
            description="This order will be cancelled. This action cannot be undone." :loading="cancelLoading"
            @confirm="handleCancel" />

    </CustomerLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import DeleteConfirmModal from '../../components/modals/DeleteConfirmModal.vue';
import { useOrderStore } from '../../stores/orderStore';
import { ROUTE_NAMES } from '../../utils/constants';
import { formatCurrency, formatDate, getImageUrl } from '../../utils/helper';

const router = useRouter();
const orderStore = useOrderStore();
const { orders, totalOrders, loading, error } = storeToRefs(orderStore);

const pageSize = ref(10);
const currentPage = ref(0);

const showDetailModal = ref(false);
const showCancelModal = ref(false);
const cancelLoading = ref(false);
const selectedOrder = ref(null);

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

const openDetail = (order) => {
    selectedOrder.value = order;
    showDetailModal.value = true;
};

const openCancelModal = (order) => {
    selectedOrder.value = order;
    showCancelModal.value = true;
};

const handleCancel = async () => {
    cancelLoading.value = true;
    try {
        await orderStore.cancelOrder(selectedOrder.value.id);
        showCancelModal.value = false;
    } catch {
        // error handled in store
    } finally {
        cancelLoading.value = false;
    }
};

const changePage = (page) => {
    currentPage.value = page;
    orderStore.fetchOrders(page, pageSize.value);
};

onMounted(() => {
    orderStore.fetchOrders(0, pageSize.value);
});
</script>
