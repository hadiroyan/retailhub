<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Suppliers</h1>
                <p class="text-sm text-gray-500 mt-0.5">Manage your store suppliers</p>
            </div>
            <button v-if="canWrite" @click="openCreate"
                class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                <i class="fas fa-plus"></i>
                <span>Add Supplier</span>
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
        <div v-else-if="suppliers.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
            <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                <i class="fas fa-truck text-blue-400 text-2xl"></i>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">No suppliers yet</h3>
            <p class="text-sm text-gray-500 mb-4">Add your first supplier to get started</p>
            <button v-if="canWrite" @click="openCreate"
                class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                Add Supplier
            </button>
        </div>

        <!-- Supplier table -->
        <div v-else class="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b border-gray-200">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Supplier</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Contact Person</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Phone</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Email</th>
                        <th v-if="canWrite" class="text-left px-4 py-3 font-medium text-gray-600">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                    <tr v-for="supplier in suppliers" :key="supplier.id" class="hover:bg-gray-50 transition-colors">
                        <td class="px-4 py-3">
                            <p class="font-medium text-gray-900">{{ supplier.name }}</p>
                            <p v-if="supplier.address" class="text-xs text-gray-400 truncate max-w-48">
                                {{ supplier.address }}
                            </p>
                        </td>
                        <td class="px-4 py-3 text-gray-500">{{ supplier.contactPerson || '-' }}</td>
                        <td class="px-4 py-3 text-gray-500">{{ supplier.phone || '-' }}</td>
                        <td class="px-4 py-3 text-gray-500">{{ supplier.email || '-' }}</td>
                        <td v-if="canWrite" class="px-4 py-3">
                            <div class="flex items-center gap-1">
                                <button @click="openEdit(supplier)"
                                    class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                    title="Edit">
                                    <i class="fas fa-pen text-xs"></i>
                                </button>
                                <button @click="openDelete(supplier)"
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
        <div v-if="totalSuppliers > pageSize"
            class="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
            <p class="text-sm text-gray-500">Showing {{ suppliers.length }} of {{ totalSuppliers }} suppliers</p>
            <div class="flex gap-2">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button @click="changePage(currentPage + 1)" :disabled="(currentPage + 1) * pageSize >= totalSuppliers"
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
                        {{ isEdit ? 'Edit Supplier' : 'Add Supplier' }}
                    </h3>

                    <div class="space-y-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">
                                Name <span class="text-red-500">*</span>
                            </label>
                            <input v-model="form.name" type="text" placeholder="e.g. PT Supplier ABC"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Contact Person</label>
                            <input v-model="form.contactPerson" type="text" placeholder="e.g. John Doe"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>
                        <div class="grid grid-cols-2 gap-3">
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                                <input v-model="form.phone" type="text" placeholder="08123456789"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                                <input v-model="form.email" type="email" placeholder="supplier@email.com"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                            </div>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Address</label>
                            <textarea v-model="form.address" rows="2" placeholder="Supplier address"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"></textarea>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Notes</label>
                            <textarea v-model="form.notes" rows="2" placeholder="Optional notes"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"></textarea>
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
                                <i class="fas fa-spinner animate-spin mr-1"></i>Saving...
                            </span>
                            <span v-else>{{ isEdit ? 'Save Changes' : 'Add Supplier' }}</span>
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

        <!-- Delete modal -->
        <DeleteConfirmModal v-model="showDeleteModal" :title="`Delete &quot;${selectedSupplier?.name}&quot;?`"
            description="This supplier will be permanently deleted." :loading="deleteLoading" @confirm="handleDelete" />

    </DashboardLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import DashboardLayout from '../../layouts/DashboardLayout.vue';
import DeleteConfirmModal from '../../components/modals/DeleteConfirmModal.vue';
import { useSupplierStore } from '../../stores/supplierStore';
import { useAuthStore } from '../../stores/auth';
import { ROLES } from '../../utils/constants';

const route = useRoute();
const supplierStore = useSupplierStore();
const authStore = useAuthStore();

const { suppliers, totalSuppliers, loading, error } = storeToRefs(supplierStore);
const { userRole } = storeToRefs(authStore);

const storeId = computed(() => route.params.storeId || supplierStore.activeStore?.id);
const pageSize = ref(10);
const currentPage = ref(0);

const canWrite = computed(() =>
    [ROLES.OWNER, ROLES.ADMIN].includes(userRole.value)
);

const fetchSuppliers = () => {
    supplierStore.fetchSuppliers(storeId.value, {
        page: currentPage.value,
        size: pageSize.value,
    });
};

const changePage = (page) => {
    currentPage.value = page;
    fetchSuppliers();
};

// Form state
const showFormModal = ref(false);
const formLoading = ref(false);
const formError = ref(null);
const isEdit = ref(false);
const selectedSupplier = ref(null);

const defaultForm = () => ({
    name: '',
    contactPerson: '',
    phone: '',
    email: '',
    address: '',
    notes: '',
});

const form = ref(defaultForm());

const openCreate = () => {
    isEdit.value = false;
    selectedSupplier.value = null;
    form.value = defaultForm();
    formError.value = null;
    showFormModal.value = true;
};

const openEdit = (supplier) => {
    isEdit.value = true;
    selectedSupplier.value = supplier;
    form.value = {
        name: supplier.name,
        contactPerson: supplier.contactPerson || '',
        phone: supplier.phone || '',
        email: supplier.email || '',
        address: supplier.address || '',
        notes: supplier.notes || '',
    };
    formError.value = null;
    showFormModal.value = true;
};

const closeFormModal = () => {
    showFormModal.value = false;
    formError.value = null;
};

const handleSubmit = async () => {
    if (!form.value.name?.trim()) {
        formError.value = 'Supplier name is required';
        return;
    }
    formLoading.value = true;
    formError.value = null;
    try {
        const data = { ...form.value };
        if (!data.contactPerson) delete data.contactPerson;
        if (!data.phone) delete data.phone;
        if (!data.email) delete data.email;
        if (!data.address) delete data.address;
        if (!data.notes) delete data.notes;

        if (isEdit.value) {
            await supplierStore.updateSupplier(storeId.value, selectedSupplier.value.id, data);
        } else {
            await supplierStore.createSupplier(storeId.value, data);
        }
        closeFormModal();
        fetchSuppliers();
    } catch (err) {
        formError.value = err.response?.data?.message || 'Failed to save supplier';
    } finally {
        formLoading.value = false;
    }
};

// Delete
const showDeleteModal = ref(false);
const deleteLoading = ref(false);

const openDelete = (supplier) => {
    selectedSupplier.value = supplier;
    showDeleteModal.value = true;
};

const handleDelete = async () => {
    deleteLoading.value = true;
    try {
        await supplierStore.deleteSupplier(storeId.value, selectedSupplier.value.id);
        showDeleteModal.value = false;
        fetchSuppliers();
    } finally {
        deleteLoading.value = false;
    }
};

onMounted(() => {
    fetchSuppliers();
});
</script>