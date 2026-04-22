<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Employees</h1>
                <p class="text-sm text-gray-500 mt-0.5">Manage your store employees</p>
            </div>
            <button v-if="canWrite" @click="showCreateModal = true"
                class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                <i class="fas fa-plus"></i>
                <span>Add Employee</span>
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
        <div v-else-if="employees.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
            <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                <i class="fas fa-users text-blue-400 text-2xl"></i>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">No employees yet</h3>
            <p class="text-sm text-gray-500 mb-4">Add your first employee to get started</p>
            <button v-if="canWrite" @click="showCreateModal = true"
                class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                Add Employee
            </button>
        </div>

        <!-- Employee table -->
        <div v-else class="bg-white rounded-xl border border-gray-200 overflow-hidden">
            <table class="w-full text-sm">
                <thead class="bg-gray-50 border-b border-gray-200">
                    <tr>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Name</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Email</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Role</th>
                        <th class="text-left px-4 py-3 font-medium text-gray-600">Verified</th>
                        <th v-if="canWrite" class="text-left px-4 py-3 font-medium text-gray-600">Actions</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                    <tr v-for="employee in employees" :key="employee.id" class="hover:bg-gray-50 transition-colors">
                        <td class="px-4 py-3 font-medium text-gray-900">{{ employee.fullName }}</td>
                        <td class="px-4 py-3 text-gray-500">{{ employee.email }}</td>
                        <td class="px-4 py-3">
                            <span class="text-xs px-2 py-0.5 rounded-full font-medium"
                                :class="roleBadgeClass(employee.role)">
                                {{ employee.role }}
                            </span>
                        </td>
                        <td class="px-4 py-3">
                            <span v-if="employee.emailVerified" class="text-green-600">
                                <i class="fas fa-check-circle"></i>
                            </span>
                            <span v-else class="text-gray-300">
                                <i class="fas fa-times-circle"></i>
                            </span>
                        </td>
                        <td v-if="canWrite" class="px-4 py-3">
                            <div class="flex items-center gap-1">
                                <button @click="openEditRole(employee)"
                                    class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                    title="Edit role">
                                    <i class="fas fa-pen text-xs"></i>
                                </button>
                                <button @click="openDelete(employee)"
                                    class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                    title="Remove employee">
                                    <i class="fas fa-trash text-xs"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Pagination -->
        <div v-if="totalEmployees > pageSize"
            class="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
            <p class="text-sm text-gray-500">Showing {{ employees.length }} of {{ totalEmployees }} employees</p>
            <div class="flex gap-2">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button @click="changePage(currentPage + 1)" :disabled="(currentPage + 1) * pageSize >= totalEmployees"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- Create employee modal -->
        <Teleport to="body">
            <div v-if="showCreateModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                @click.self="showCreateModal = false">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
                    <h3 class="text-lg font-semibold text-gray-900 mb-4">Add Employee</h3>

                    <div class="space-y-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Full Name <span
                                    class="text-red-500">*</span></label>
                            <input v-model="createForm.fullName" type="text" placeholder="John Doe"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Email <span
                                    class="text-red-500">*</span></label>
                            <input v-model.trim="createForm.email" type="email" placeholder="employee@example.com"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Password <span
                                    class="text-red-500">*</span></label>
                            <input v-model="createForm.password" type="password" placeholder="Min 8 characters"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Role <span
                                    class="text-red-500">*</span></label>
                            <select v-model="createForm.role"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                                <option value="">Select role</option>
                                <option v-for="role in assignableRoles" :key="role" :value="role">{{ role }}</option>
                            </select>
                        </div>

                        <div v-if="createError"
                            class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                            {{ createError }}
                        </div>
                    </div>

                    <div class="flex gap-3 mt-5">
                        <button @click="showCreateModal = false"
                            class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                            Cancel
                        </button>
                        <button @click="handleCreate" :disabled="createLoading"
                            class="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                            <span v-if="createLoading"><i class="fas fa-spinner fa-spin mr-1"></i>Adding...</span>
                            <span v-else>Add Employee</span>
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

        <!-- Edit role modal -->
        <Teleport to="body">
            <div v-if="showEditModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                @click.self="showEditModal = false">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-sm p-6">
                    <h3 class="text-lg font-semibold text-gray-900 mb-4">Update Role</h3>
                    <p class="text-sm text-gray-500 mb-4">
                        Updating role for <span class="font-medium text-gray-800">{{ selectedEmployee?.fullName
                            }}</span>
                    </p>

                    <select v-model="editRole"
                        class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 mb-4">
                        <option v-for="role in assignableRoles" :key="role" :value="role">{{ role }}</option>
                    </select>

                    <div v-if="editError"
                        class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600 mb-4">
                        {{ editError }}
                    </div>

                    <div class="flex gap-3">
                        <button @click="showEditModal = false"
                            class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                            Cancel
                        </button>
                        <button @click="handleEditRole" :disabled="editLoading"
                            class="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50">
                            <span v-if="editLoading"><i class="fas fa-spinner fa-spin mr-1"></i>Saving...</span>
                            <span v-else>Save</span>
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

        <!-- Delete modal -->
        <DeleteConfirmModal v-model="showDeleteModal" :title="`Remove ${selectedEmployee?.fullName}?`"
            description="This employee will be removed from the store. Their account will not be deleted."
            :loading="deleteLoading" @confirm="handleDelete" />

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
import { ROLES } from '../../utils/constants';

const route = useRoute();
const storeStore = useStoreStore();
const authStore = useAuthStore();

const { employees, totalEmployees, loading, error } = storeToRefs(storeStore);
const { userRole } = storeToRefs(authStore);

const storeId = computed(() => route.params.storeId || storeStore.activeStore?.id);
const pageSize = ref(10);
const currentPage = ref(0);

// Permission
const canWrite = computed(() =>
    [ROLES.OWNER, ROLES.ADMIN].includes(userRole.value)
);

// Assignable roles — ADMIN cannot assign ADMIN
const assignableRoles = computed(() => {
    if (userRole.value === ROLES.OWNER) return ['ADMIN', 'MANAGER', 'STAFF'];
    return ['MANAGER', 'STAFF'];
});

// Role badge
const roleBadgeClass = (role) => {
    const map = {
        ADMIN: 'bg-sky-100 text-sky-700',
        MANAGER: 'bg-green-100 text-green-700',
        STAFF: 'bg-emerald-100 text-emerald-700',
    };
    return map[role] || 'bg-gray-100 text-gray-600';
};

// Create
const showCreateModal = ref(false);
const createLoading = ref(false);
const createError = ref(null);
const createForm = ref({ fullName: '', email: '', password: '', role: '' });

const handleCreate = async () => {
    createError.value = null;
    if (!createForm.value.fullName || !createForm.value.email ||
        !createForm.value.password || !createForm.value.role) {
        createError.value = 'All fields are required';
        return;
    }
    createLoading.value = true;
    try {
        await storeStore.createEmployee(storeId.value, createForm.value);
        showCreateModal.value = false;
        createForm.value = { fullName: '', email: '', password: '', role: '' };
    } catch (err) {
        createError.value = err.response?.data?.message || 'Failed to add employee';
    } finally {
        createLoading.value = false;
    }
};

// Edit role
const showEditModal = ref(false);
const editLoading = ref(false);
const editError = ref(null);
const editRole = ref('');
const selectedEmployee = ref(null);

const openEditRole = (employee) => {
    selectedEmployee.value = employee;
    editRole.value = employee.role;
    showEditModal.value = true;
};

const handleEditRole = async () => {
    editError.value = null;
    editLoading.value = true;
    try {
        await storeStore.updateEmployeeRole(storeId.value, selectedEmployee.value.id, { role: editRole.value });
        showEditModal.value = false;
    } catch (err) {
        editError.value = err.response?.data?.message || 'Failed to update role';
    } finally {
        editLoading.value = false;
    }
};

// Delete
const showDeleteModal = ref(false);
const deleteLoading = ref(false);

const openDelete = (employee) => {
    selectedEmployee.value = employee;
    showDeleteModal.value = true;
};

const handleDelete = async () => {
    deleteLoading.value = true;
    try {
        await storeStore.removeEmployee(storeId.value, selectedEmployee.value.id);
        showDeleteModal.value = false;
    } finally {
        deleteLoading.value = false;
    }
};

const changePage = (page) => {
    currentPage.value = page;
    storeStore.fetchEmployees(storeId.value, page, pageSize.value);
};

onMounted(() => {
    storeStore.fetchEmployees(storeId.value, 0, pageSize.value);
});
</script>