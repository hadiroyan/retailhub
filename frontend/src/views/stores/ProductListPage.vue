<template>
    <DashboardLayout>
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Products</h1>
                <p class="text-sm text-gray-500 mt-0.5">Manage your store products</p>
            </div>
            <button v-if="canWrite" @click="openCreate"
                class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                <i class="fas fa-plus"></i>
                <span>Add Product</span>
            </button>
        </div>

        <!-- Filters -->
        <div class="flex flex-col sm:flex-row gap-3 mb-4">
            <input v-model="searchName" type="text" placeholder="Search by name..."
                class="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                @input="onSearchInput" />
            <select v-model="filterCategory"
                class="px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                @change="fetchWithFilters">
                <option value="">All Categories</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
            <select v-model="sortByPrice"
                class="px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                @change="fetchWithFilters">
                <option value="">Default Sort</option>
                <option value="asc">Price: Low to High</option>
                <option value="desc">Price: High to Low</option>
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
        <div v-else-if="products.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
            <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                <i class="fas fa-box text-blue-400 text-2xl"></i>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">No products yet</h3>
            <p class="text-sm text-gray-500 mb-4">Add your first product to get started</p>
            <button v-if="canWrite" @click="openCreate"
                class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                Add Product
            </button>
        </div>

        <!-- Product table -->
        <div v-else class="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b border-gray-200">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Product</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">SKU</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Category</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Price</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Cost Price</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Stock</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                        <th v-if="canWrite" class="text-left px-4 py-3 font-medium text-gray-600">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                    <tr v-for="product in products" :key="product.id" class="hover:bg-gray-50 transition-colors">
                        <td class="px-4 py-3">
                            <p class="font-medium text-gray-900">{{ product.name }}</p>
                            <p v-if="product.description" class="text-xs text-gray-400 truncate max-w-48">{{
                                product.description }}</p>
                        </td>
                        <td class="px-4 py-3 font-mono text-xs text-gray-500">{{ product.sku }}</td>
                        <td class="px-4 py-3 text-gray-500">{{ product.category?.name || '-' }}</td>
                        <td class="px-4 py-3 font-medium text-gray-900">{{ formatCurrency(product.price) }}</td>
                        <td class="px-4 py-3 text-gray-500">
                            {{ product.costPrice ? formatCurrency(product.costPrice) : '-' }}</td>
                        <td class="px-4 py-3">
                            <span :class="product.stockQuantity <= 0 ? 'text-red-600 font-medium' : 'text-gray-700'">
                                {{ product.stockQuantity }}
                            </span>
                        </td>
                        <td class="px-4 py-3">
                            <span class="text-xs px-2 py-0.5 rounded-full font-medium"
                                :class="statusBadgeClass(product.status)">
                                {{ product.status }}
                            </span>
                        </td>
                        <td v-if="canWrite" class="px-4 py-3">
                            <div class="flex items-center gap-1">
                                <button @click="openEdit(product)"
                                    class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                    title="Edit">
                                    <i class="fas fa-pen text-xs"></i>
                                </button>
                                <button @click="openDelete(product)"
                                    class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                    title="Delete">
                                    <i class="fas fa-trash text-xs"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalProducts > pageSize"
            class="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
            <p class="text-sm text-gray-500">Showing {{ products.length }} of {{ totalProducts }} products</p>
            <div class="flex gap-2">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button @click="changePage(currentPage + 1)" :disabled="(currentPage + 1) * pageSize >= totalProducts"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- Create/Edit modal -->
        <Teleport to="body">
            <div v-if="showFormModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                @click.self="closeFormModal">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
                    <h3 class="text-lg font-semibold text-gray-900 mb-4">
                        {{ isEdit ? 'Edit Product' : 'Add Product' }}
                    </h3>

                    <div class="space-y-4">
                        <!-- SKU — only on create -->
                        <div v-if="!isEdit">
                            <label class="block text-sm font-medium text-gray-700 mb-1">SKU <span
                                    class="text-red-500">*</span></label>
                            <input v-model="form.sku" type="text" placeholder="e.g. PROD-001"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Name <span
                                    class="text-red-500">*</span></label>
                            <input v-model="form.name" type="text" placeholder="Product name"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
                            <textarea v-model="form.description" rows="2" placeholder="Optional description"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"></textarea>
                        </div>

                        <div class="grid grid-cols-2 gap-3">
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Price <span
                                        class="text-red-500">*</span></label>
                                <input v-model.number="form.price" type="number" min="0" placeholder="0"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Cost Price</label>
                                <input v-model.number="form.costPrice" type="number" min="0" placeholder="0"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                            </div>
                        </div>

                        <div class="grid grid-cols-2 gap-3">
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Stock</label>
                                <input v-model.number="form.stockQuantity" type="number" min="0" placeholder="0"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Min Stock</label>
                                <input v-model.number="form.minStockLevel" type="number" min="0" placeholder="10"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                            </div>
                        </div>

                        <div class="grid grid-cols-2 gap-3">
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Category</label>
                                <select v-model="form.categoryId"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                                    <option value="">No Category</option>
                                    <option v-for="cat in allCategories" :key="cat.id" :value="cat.id">{{ cat.name }}
                                    </option>
                                </select>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
                                <select v-model="form.status"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                                    <option value="ACTIVE">ACTIVE</option>
                                    <option value="DRAFT">DRAFT</option>
                                    <option value="OUT_OF_STOCK">OUT_OF_STOCK</option>
                                </select>
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
                            <span v-if="formLoading"><i class="fas fa-spinner fa-spin mr-1"></i>Saving...</span>
                            <span v-else>{{ isEdit ? 'Save Changes' : 'Add Product' }}</span>
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

        <!-- Delete modal -->
        <DeleteConfirmModal v-model="showDeleteModal" :title="`Delete &quot;${selectedProduct?.name}&quot;?`"
            description="This product will be permanently deleted." :loading="deleteLoading" @confirm="handleDelete" />

    </DashboardLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import DeleteConfirmModal from '../../components/modals/DeleteConfirmModal.vue';
import { useStoreStore } from '../../stores/storeStore';
import { useAuthStore } from '../../stores/auth';
import { ROLES, PRODUCT_STATUS } from '../../utils/constants';
import { formatCurrency, debounce } from '../../utils/helper';

const route = useRoute();
const storeStore = useStoreStore();
const authStore = useAuthStore();

const { products, totalProducts, categories, loading, error } = storeToRefs(storeStore);
const { userRole } = storeToRefs(authStore);

const storeId = computed(() => route.params.storeId || storeStore.activeStore?.id);
const pageSize = ref(10);
const currentPage = ref(0);

// Filters
const searchName = ref('');
const filterCategory = ref('');
const sortByPrice = ref('');

const canWrite = computed(() =>
    [ROLES.OWNER, ROLES.ADMIN, ROLES.MANAGER, ROLES.STAFF].includes(userRole.value)
);

// Flatten categories for select (root + children)
const allCategories = computed(() => {
    const result = [];
    categories.value.forEach(cat => {
        result.push(cat);
        if (cat.children) cat.children.forEach(child => result.push(child));
    });
    return result;
});

// Status badge
const statusBadgeClass = (status) => {
    const map = {
        ACTIVE: 'bg-green-100 text-green-700',
        DRAFT: 'bg-gray-100 text-gray-600',
        OUT_OF_STOCK: 'bg-red-100 text-red-700',
    };
    return map[status] || 'bg-gray-100 text-gray-600';
};

const fetchWithFilters = () => {
    currentPage.value = 0;
    storeStore.fetchInternalProducts(storeId.value, {
        name: searchName.value || undefined,
        categoryId: filterCategory.value || undefined,
        sortByPrice: sortByPrice.value || undefined,
        page: 0,
        size: pageSize.value,
    });
};

const onSearchInput = debounce(fetchWithFilters, 400);

// Form state
const showFormModal = ref(false);
const formLoading = ref(false);
const formError = ref(null);
const isEdit = ref(false);
const selectedProduct = ref(null);

const defaultForm = () => ({
    sku: '', name: '', description: '',
    price: null, costPrice: null,
    stockQuantity: 0, minStockLevel: 10,
    categoryId: '', status: 'ACTIVE',
});

const form = ref(defaultForm());

const openCreate = () => {
    isEdit.value = false;
    selectedProduct.value = null;
    form.value = defaultForm();
    formError.value = null;
    showFormModal.value = true;
};

const openEdit = (product) => {
    isEdit.value = true;
    selectedProduct.value = product;
    form.value = {
        name: product.name,
        description: product.description || '',
        price: product.price,
        costPrice: product.costPrice || null,
        stockQuantity: product.stockQuantity,
        minStockLevel: product.minStockLevel || 0,
        categoryId: product.category?.id || '',
        status: product.status,
    };
    formError.value = null;
    showFormModal.value = true;
};

const closeFormModal = () => {
    showFormModal.value = false;
    formError.value = null;
};

const handleSubmit = async () => {
    if (!form.value.name?.trim() || !form.value.price) {
        formError.value = 'Name and price are required';
        return;
    }
    if (!isEdit.value && !form.value.sku?.trim()) {
        formError.value = 'SKU is required';
        return;
    }
    formLoading.value = true;
    formError.value = null;
    try {
        const data = { ...form.value };
        if (!data.categoryId) delete data.categoryId;
        if (!data.costPrice) delete data.costPrice;

        if (isEdit.value) {
            await storeStore.updateProduct(storeId.value, selectedProduct.value.id, data);
        } else {
            await storeStore.createProduct(storeId.value, data);
        }
        closeFormModal();
        fetchWithFilters();
    } catch (err) {
        formError.value = err.response?.data?.message || 'Failed to save product';
    } finally {
        formLoading.value = false;
    }
};

// Delete
const showDeleteModal = ref(false);
const deleteLoading = ref(false);

const openDelete = (product) => {
    selectedProduct.value = product;
    showDeleteModal.value = true;
};

const handleDelete = async () => {
    deleteLoading.value = true;
    try {
        await storeStore.deleteProduct(storeId.value, selectedProduct.value.id);
        showDeleteModal.value = false;
        fetchWithFilters();
    } finally {
        deleteLoading.value = false;
    }
};

const changePage = (page) => {
    currentPage.value = page;
    storeStore.fetchInternalProducts(storeId.value, {
        name: searchName.value || undefined,
        categoryId: filterCategory.value || undefined,
        sortByPrice: sortByPrice.value || undefined,
        page,
        size: pageSize.value,
    });
};

onMounted(async () => {
    await storeStore.fetchCategories(storeId.value, 0, 100);
    fetchWithFilters();
});
</script>