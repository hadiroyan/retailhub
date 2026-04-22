<template>
    <DashboardLayout>

        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h1 class="text-2xl font-bold text-gray-900">Categories</h1>
                <p class="text-sm text-gray-500 mt-0.5">Manage your store categories</p>
            </div>
            <button v-if="canWrite" @click="openCreate(null)"
                class="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                <i class="fas fa-plus"></i>
                <span>Add Category</span>
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
        <div v-else-if="categories.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
            <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                <i class="fas fa-folder text-blue-400 text-2xl"></i>
            </div>
            <h3 class="font-semibold text-gray-900 mb-1">No categories yet</h3>
            <p class="text-sm text-gray-500 mb-4">Create your first category to organize products</p>
            <button v-if="canWrite" @click="openCreate(null)"
                class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                Add Category
            </button>
        </div>

        <!-- Category list -->
        <div v-else class="space-y-3">
            <div v-for="category in categories" :key="category.id"
                class="bg-white rounded-xl border border-gray-200 p-4">
                <!-- Root category -->
                <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center shrink-0">
                            <i class="fas fa-folder text-blue-500 text-sm"></i>
                        </div>
                        <div>
                            <p class="font-medium text-gray-900">{{ category.name }}</p>
                            <p class="text-xs text-gray-400">{{ category.slug }} · {{ category.productCount }} products
                            </p>
                        </div>
                    </div>
                    <div v-if="canWrite" class="flex items-center gap-1">
                        <button @click="openCreate(category)"
                            class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            title="Add subcategory">
                            <i class="fas fa-plus text-xs"></i>
                        </button>
                        <button @click="openEdit(category)""
                            class=" p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg
                            transition-colors" title="Edit">
                            <i class="fas fa-pen text-xs"></i>
                        </button>
                        <button @click="openDelete(category)"
                            class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                            title="Delete">
                            <i class="fas fa-trash text-xs"></i>
                        </button>
                    </div>
                </div>

                <!-- Children -->
                <div v-if="category.children && category.children.length > 0" class="mt-3 ml-11 space-y-2">
                    <div v-for="child in category.children" :key="child.id"
                        class="flex items-center justify-between p-2 bg-gray-50 rounded-lg">
                        <div class="flex items-center gap-2">
                            <i class="fas fa-folder text-gray-400 text-xs"></i>
                            <div>
                                <p class="text-sm font-medium text-gray-700">{{ child.name }}</p>
                                <p class="text-xs text-gray-400">{{ child.slug }} · {{ child.productCount }} products
                                </p>
                            </div>
                        </div>
                        <div v-if="canWrite" class="flex items-center gap-1">
                            <button @click="openEdit(child, category)"
                                class="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                title="Edit">
                                <i class="fas fa-pen text-xs"></i>
                            </button>
                            <button @click="openDelete(child)"
                                class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                title="Delete">
                                <i class="fas fa-trash text-xs"></i>
                            </button>
                        </div>
                    </div>
                </div>

            </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalCategories > pageSize"
            class="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
            <p class="text-sm text-gray-500">Showing {{ categories.length }} of {{ totalCategories }} categories</p>
            <div class="flex gap-2">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button @click="changePage(currentPage + 1)" :disabled="(currentPage + 1) * pageSize >= totalCategories"
                    class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- Create/Edit modal -->
        <Teleport to="body">
            <div v-if="showFormModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
                @click.self="closeFormModal">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
                    <h3 class="text-lg font-semibold text-gray-900 mb-4">
                        {{ isEdit ? 'Edit Category' : parentCategory ? `Add Subcategory in "${parentCategory.name}"` :
                            'Add Category' }}
                    </h3>

                    <div class="space-y-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Name <span
                                    class="text-red-500">*</span></label>
                            <input v-model="form.name" type="text" placeholder="e.g. Electronics"
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
                            <textarea v-model="form.description" rows="2" placeholder="Optional description"
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
                            <span v-if="formLoading"><i class="fas fa-spinner fa-spin mr-1"></i>Saving...</span>
                            <span v-else>{{ isEdit ? 'Save Changes' : 'Add Category' }}</span>
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

        <!-- Delete modal -->
        <DeleteConfirmModal v-model="showDeleteModal" :title="`Delete &quot;${selectedCategory?.name}&quot;?`"
            description="This category will be deleted. Products in this category will have their category removed."
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

const { categories, totalCategories, loading, error } = storeToRefs(storeStore);
const { userRole } = storeToRefs(authStore);

const storeId = computed(() => route.params.storeId || storeStore.activeStore?.id);
const pageSize = ref(10);
const currentPage = ref(0);

const canWrite = computed(() =>
    [ROLES.OWNER, ROLES.ADMIN, ROLES.MANAGER].includes(userRole.value)
);

// Form state
const showFormModal = ref(false);
const formLoading = ref(false);
const formError = ref(null);
const isEdit = ref(false);
const parentCategory = ref(null);
const selectedCategory = ref(null);

const form = ref({ name: '', description: '' });

const openCreate = (parent) => {
    isEdit.value = false;
    parentCategory.value = parent;
    selectedCategory.value = null;
    form.value = { name: '', description: '' };
    formError.value = null;
    showFormModal.value = true;
};

const openEdit = (category, parent = null) => {
    console.log('parent: ', parent);
    console.log('category: ', category);
    isEdit.value = true;
    selectedCategory.value = category;
    selectedCategory.value.parent = parent; 
    parentCategory.value = parent;
    form.value = { name: category.name, description: category.description || '' };
    formError.value = null;
    showFormModal.value = true;
};

const closeFormModal = () => {
    showFormModal.value = false;
    formError.value = null;
};

const handleSubmit = async () => {
    if (!form.value.name.trim()) {
        formError.value = 'Category name is required';
        return;
    }
    formLoading.value = true;
    formError.value = null;
    try {
        const data = { ...form.value };
        if (parentCategory.value) data.parentId = parentCategory.value.id;

        if (isEdit.value) {
            await storeStore.updateCategory(storeId.value, selectedCategory.value.id, data);
        } else {
            await storeStore.createCategory(storeId.value, data);
        }

        closeFormModal();
        storeStore.fetchCategories(storeId.value, currentPage.value, pageSize.value);
    } catch (err) {
        formError.value = err.response?.data?.message || 'Failed to save category';
    } finally {
        formLoading.value = false;
    }
};

// Delete
const showDeleteModal = ref(false);
const deleteLoading = ref(false);

const openDelete = (category) => {
    selectedCategory.value = category;
    showDeleteModal.value = true;
};

const handleDelete = async () => {
    deleteLoading.value = true;
    try {
        await storeStore.deleteCategory(storeId.value, selectedCategory.value.id);
        showDeleteModal.value = false;
        storeStore.fetchCategories(storeId.value, currentPage.value, pageSize.value);
    } finally {
        deleteLoading.value = false;
    }
};

const changePage = (page) => {
    currentPage.value = page;
    storeStore.fetchCategories(storeId.value, page, pageSize.value);
};

onMounted(() => {
    storeStore.fetchCategories(storeId.value, 0, pageSize.value);
});
</script>