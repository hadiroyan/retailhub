<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Purchase Orders</h1>
                <p class="text-sm text-gray-500 mt-0.5">Manage restock orders from suppliers</p>
            </div>
            <div class="flex items-center gap-3">
                <!-- Filter status -->
                <select v-model="filterStatus" @change="fetchWithFilters"
                    class="px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">All Status</option>
                    <option value="PENDING">PENDING</option>
                    <option value="CONFIRMED">CONFIRMED</option>
                    <option value="RECEIVED">RECEIVED</option>
                    <option value="CANCELLED">CANCELLED</option>
                </select>
                <button v-if="canWrite" @click="openCreate"
                    class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                    <i class="fas fa-plus"></i>
                    <span>Create PO</span>
                </button>
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
        <div v-else-if="purchaseOrders.length === 0"
            class="flex flex-col items-center justify-center py-20 text-center">
            <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                <i class="fas fa-file-invoice text-blue-400 text-2xl"></i>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">No purchase orders yet</h3>
            <p class="text-sm text-gray-500 mb-4">Create a purchase order to restock your products</p>
            <button v-if="canWrite" @click="openCreate"
                class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                Create PO
            </button>
        </div>

        <!-- Purchase order table -->
        <div v-else class="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b border-gray-200">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Order</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Supplier</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Items</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Total</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Expected Delivery</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                    <tr v-for="order in purchaseOrders" :key="order.id" class="hover:bg-gray-50 transition-colors">
                        <td class="px-4 py-3">
                            <p class="font-mono text-xs font-medium text-gray-900">{{ order.orderNumber }}</p>
                            <p class="text-xs text-gray-400">{{ formatDate(order.createdAt) }}</p>
                        </td>
                        <td class="px-4 py-3">
                            <p class="text-sm font-medium text-gray-900">{{ order.supplier?.name }}</p>
                            <p v-if="order.supplier?.contactPerson" class="text-xs text-gray-400">
                                {{ order.supplier.contactPerson }}
                            </p>
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
                        <td class="px-4 py-3 text-gray-500">
                            {{ order.expectedDeliveryDate ? formatDate(order.expectedDeliveryDate) : '-' }}
                        </td>
                        <td class="px-4 py-3">
                            <span class="text-xs px-2 py-0.5 rounded-full font-medium"
                                :class="statusBadgeClass(order.status)">
                                {{ order.status }}
                            </span>
                        </td>
                        <td class="px-4 py-3">
                            <div class="flex items-center gap-1">
                                <button @click="openDetail(order)"
                                    class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                    title="View detail">
                                    <i class="fas fa-eye text-xs"></i>
                                </button>
                                <button v-if="canWrite && order.status !== 'RECEIVED' && order.status !== 'CANCELLED'"
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
        <div v-if="totalPurchaseOrders > pageSize"
            class="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
            <p class="text-sm text-gray-500">
                Showing {{ purchaseOrders.length }} of {{ totalPurchaseOrders }} orders
            </p>
            <div class="flex gap-2">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button @click="changePage(currentPage + 1)"
                    :disabled="(currentPage + 1) * pageSize >= totalPurchaseOrders"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- Create PO modal -->
        <Teleport to="body">
            <div v-if="showFormModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                @click.self="closeFormModal">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-2xl p-6 max-h-[90vh] overflow-y-auto">
                    <h3 class="text-lg font-semibold text-gray-900 mb-4">Create Purchase Order</h3>

                    <div class="space-y-4">
                        <!-- Supplier -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">
                                Supplier <span class="text-red-500">*</span>
                            </label>
                            <select v-model="form.supplierId"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                                <option value="">Select supplier</option>
                                <option v-for="supplier in suppliers" :key="supplier.id" :value="supplier.id">
                                    {{ supplier.name }}
                                </option>
                            </select>
                        </div>

                        <!-- Expected delivery date -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">
                                Expected Delivery Date
                            </label>
                            <input v-model="form.expectedDeliveryDate" type="date"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>

                        <!-- Notes -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                            <textarea v-model="form.notes" rows="2" placeholder="Optional notes"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"></textarea>
                        </div>

                        <!-- Items -->
                        <div>
                            <div class="flex items-center justify-between mb-2">
                                <label class="block text-sm font-medium text-gray-700">
                                    Items <span class="text-red-500">*</span>
                                </label>
                                <button @click="addItem" type="button"
                                    class="text-xs text-blue-600 hover:text-blue-700 font-medium">
                                    <i class="fas fa-plus mr-1"></i>Add Item
                                </button>
                            </div>

                            <div class="space-y-2">
                                <div v-for="(item, index) in form.items" :key="index"
                                    class="grid grid-cols-12 gap-2 items-center">
                                    <!-- Product select -->
                                    <div class="col-span-5">
                                        <select v-model="item.productId"
                                            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                                            <option value="">Select product</option>
                                            <option v-for="product in products" :key="product.id" :value="product.id">
                                                {{ product.name }}
                                            </option>
                                        </select>
                                    </div>
                                    <!-- Quantity -->
                                    <div class="col-span-3">
                                        <input v-model.number="item.quantity" type="number" min="1" placeholder="Qty"
                                            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                                    </div>
                                    <!-- Unit price -->
                                    <div class="col-span-3">
                                        <input v-model.number="item.unitPrice" type="number" min="0" placeholder="Price"
                                            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                                    </div>
                                    <!-- Remove -->
                                    <div class="col-span-1 flex justify-center">
                                        <button @click="removeItem(index)" type="button"
                                            class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                                            <i class="fas fa-times text-xs"></i>
                                        </button>
                                    </div>
                                </div>

                                <!-- Column headers -->
                                <div v-if="form.items.length > 0"
                                    class="grid grid-cols-12 gap-2 text-xs text-gray-400 px-1">
                                    <div class="col-span-5">Product</div>
                                    <div class="col-span-3">Quantity</div>
                                    <div class="col-span-3">Unit Price</div>
                                </div>
                            </div>

                            <!-- Estimated total -->
                            <div v-if="estimatedTotal > 0"
                                class="flex justify-between items-center mt-3 pt-3 border-t border-gray-200">
                                <span class="text-sm text-gray-500">Estimated Total</span>
                                <span class="text-sm font-semibold text-gray-900">
                                    {{ formatCurrency(estimatedTotal) }}
                                </span>
                            </div>
                        </div>

                        <div v-if="formError"
                            class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                            {{ formError }}
                        </div>
                    </div>

                    <div class="flex gap-3 mt-5">
                        <button @click="closeFormModal"
                            class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                            Cancel
                        </button>
                        <button @click="handleSubmit" :disabled="formLoading"
                            class="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                            <span v-if="formLoading">
                                <i class="fas fa-spinner animate-spin mr-1"></i>Creating...
                            </span>
                            <span v-else>Create Purchase Order</span>
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

        <!-- Detail modal -->
        <Teleport to="body">
            <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                @click.self="showDetailModal = false">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
                    <h3 class="text-lg font-semibold text-gray-900 mb-1">Purchase Order Detail</h3>
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
                            <span class="text-gray-500">Supplier</span>
                            <span class="font-medium text-gray-900">{{ selectedOrder?.supplier?.name }}</span>
                        </div>
                        <div v-if="selectedOrder?.supplier?.contactPerson" class="flex justify-between">
                            <span class="text-gray-500">Contact</span>
                            <span class="font-medium text-gray-900">
                                {{ selectedOrder.supplier.contactPerson }}
                            </span>
                        </div>
                        <div v-if="selectedOrder?.expectedDeliveryDate" class="flex justify-between">
                            <span class="text-gray-500">Expected Delivery</span>
                            <span class="font-medium text-gray-900">
                                {{ formatDate(selectedOrder.expectedDeliveryDate) }}
                            </span>
                        </div>
                        <div v-if="selectedOrder?.notes" class="flex flex-col gap-1">
                            <span class="text-gray-500">Notes</span>
                            <span class="font-medium text-gray-900">{{ selectedOrder.notes }}</span>
                        </div>
                    </div>

                    <!-- Items -->
                    <div class="border-t border-gray-200 pt-4 mb-4">
                        <p class="text-sm font-medium text-gray-700 mb-3">Items</p>
                        <div class="space-y-2">
                            <div v-for="item in selectedOrder?.items" :key="item.id"
                                class="flex items-center justify-between">
                                <div>
                                    <p class="text-sm font-medium text-gray-900">{{ item.name }}</p>
                                    <p class="text-xs text-gray-400 font-mono">{{ item.sku }}</p>
                                    <p class="text-xs text-gray-400">
                                        {{ item.quantity }} x {{ formatCurrency(item.unitPrice) }}
                                    </p>
                                </div>
                                <p class="text-sm font-semibold text-gray-900">
                                    {{ formatCurrency(item.subtotal) }}
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="flex justify-between border-t border-gray-200 pt-3 mb-5">
                        <span class="font-semibold text-gray-900">Total</span>
                        <span class="font-bold text-blue-600">
                            {{ formatCurrency(selectedOrder?.totalAmount) }}
                        </span>
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

                        <div v-if="newStatus === 'RECEIVED'"
                            class="p-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-700">
                            <i class="fas fa-info-circle mr-1"></i>
                            Stock will be automatically added when status is set to RECEIVED.
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
                            <span v-if="statusLoading">
                                <i class="fas fa-spinner animate-spin mr-1"></i>
                            </span>
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
import { useSupplierStore } from '../../stores/supplierStore';
import { useStoreStore } from '../../stores/storeStore';
import { useAuthStore } from '../../stores/auth';
import { ROLES } from '../../utils/constants';
import { formatCurrency, formatDate } from '../../utils/helper';

const route = useRoute();
const supplierStore = useSupplierStore();
const storeStore = useStoreStore();
const authStore = useAuthStore();

const { purchaseOrders, totalPurchaseOrders, suppliers, loading, error } = storeToRefs(supplierStore);
const { products } = storeToRefs(storeStore);
const { userRole } = storeToRefs(authStore);

const storeId = computed(() => route.params.storeId || storeStore.activeStore?.id);
const pageSize = ref(10);
const currentPage = ref(0);
const filterStatus = ref('');

const canWrite = computed(() =>
    [ROLES.OWNER, ROLES.ADMIN, ROLES.MANAGER].includes(userRole.value)
);

const statusBadgeClass = (status) => {
    const map = {
        PENDING: 'bg-yellow-100 text-yellow-700',
        CONFIRMED: 'bg-blue-100 text-blue-700',
        RECEIVED: 'bg-green-100 text-green-700',
        CANCELLED: 'bg-red-100 text-red-700',
    };
    return map[status] || 'bg-gray-100 text-gray-600';
};

const fetchWithFilters = () => {
    currentPage.value = 0;
    supplierStore.fetchPurchaseOrders(storeId.value, {
        status: filterStatus.value || undefined,
        page: 0,
        size: pageSize.value,
    });
};

const changePage = (page) => {
    currentPage.value = page;
    supplierStore.fetchPurchaseOrders(storeId.value, {
        status: filterStatus.value || undefined,
        page,
        size: pageSize.value,
    });
};

// Form state
const showFormModal = ref(false);
const formLoading = ref(false);
const formError = ref(null);

const defaultForm = () => ({
    supplierId: '',
    expectedDeliveryDate: '',
    notes: '',
    items: [{ productId: '', quantity: 1, unitPrice: null }],
});

const form = ref(defaultForm());

const estimatedTotal = computed(() => {
    return form.value.items.reduce((total, item) => {
        if (item.quantity && item.unitPrice) {
            return total + item.quantity * item.unitPrice;
        }
        return total;
    }, 0);
});

const addItem = () => {
    form.value.items.push({ productId: '', quantity: 1, unitPrice: null });
};

const removeItem = (index) => {
    if (form.value.items.length > 1) {
        form.value.items.splice(index, 1);
    }
};

const openCreate = () => {
    form.value = defaultForm();
    formError.value = null;
    showFormModal.value = true;
};

const closeFormModal = () => {
    showFormModal.value = false;
    formError.value = null;
};

const handleSubmit = async () => {
    formError.value = null;

    if (!form.value.supplierId) {
        formError.value = 'Please select a supplier';
        return;
    }
    if (form.value.items.some(i => !i.productId || !i.quantity || !i.unitPrice)) {
        formError.value = 'Please fill in all item fields';
        return;
    }

    formLoading.value = true;
    try {
        const data = {
            supplierId: form.value.supplierId,
            expectedDeliveryDate: form.value.expectedDeliveryDate || undefined,
            notes: form.value.notes || undefined,
            items: form.value.items.map(i => ({
                productId: i.productId,
                quantity: i.quantity,
                unitPrice: i.unitPrice,
            })),
        };
        await supplierStore.createPurchaseOrder(storeId.value, data);
        closeFormModal();
        fetchWithFilters();
    } catch (err) {
        formError.value = err.response?.data?.message || 'Failed to create purchase order';
    } finally {
        formLoading.value = false;
    }
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

const availableStatuses = computed(() => {
    if (!selectedOrder.value) return [];
    const transitions = {
        PENDING: ['CONFIRMED', 'CANCELLED'],
        CONFIRMED: ['RECEIVED', 'CANCELLED'],
    };
    return transitions[selectedOrder.value.status] || [];
});

const openStatusModal = (order) => {
    selectedOrder.value = order;
    newStatus.value = availableStatuses.value[0] || '';
    statusError.value = null;
    showStatusModal.value = true;
};

const handleUpdateStatus = async () => {
    statusLoading.value = true;
    statusError.value = null;
    try {
        await supplierStore.updatePurchaseOrderStatus(
            storeId.value,
            selectedOrder.value.id,
            { status: newStatus.value }
        );
        showStatusModal.value = false;
        fetchWithFilters();
    } catch (err) {
        statusError.value = err.response?.data?.message || 'Failed to update status';
    } finally {
        statusLoading.value = false;
    }
};

onMounted(async () => {
    // Load suppliers and products into the form
    await supplierStore.fetchSuppliers(storeId.value, { page: 0, size: 100 });
    await storeStore.fetchInternalProducts(storeId.value, { page: 0, size: 100 });
    fetchWithFilters();
});
</script>