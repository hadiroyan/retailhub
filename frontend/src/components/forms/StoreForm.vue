<template>
    <form @submit.prevent="handleSubmit" class="space-y-5">

        <!-- Name -->
        <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">
                Store Name <span class="text-red-500">*</span>
            </label>
            <input v-model="form.name" type="text" placeholder="e.g. Shoes Store Jakarta"
                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-400': errors.name }" />
            <p v-if="errors.name" class="text-xs text-red-500 mt-1">{{ errors.name }}</p>
        </div>

        <!-- Description -->
        <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea v-model="form.description" rows="3" placeholder="Brief description about your store"
                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"></textarea>
        </div>

        <!-- Address -->
        <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Address</label>
            <input v-model="form.address" type="text" placeholder="e.g. Jl. Sudirman No. 1, Jakarta"
                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
        </div>

        <!-- Phone & Email -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                <input v-model="form.phone" type="text" placeholder="e.g. 08123456789"
                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Store Email</label>
                <input v-model="form.email" type="email" placeholder="e.g. store@example.com"
                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    :class="{ 'border-red-400': errors.email }" />
                <p v-if="errors.email" class="text-xs text-red-500 mt-1">{{ errors.email }}</p>
            </div>
        </div>

        <!-- Error message -->
        <div v-if="serverError" class="p-3 bg-red-50 border border-red-200 rounded-lg">
            <p class="text-sm text-red-600">{{ serverError }}</p>
        </div>

        <!-- Actions -->
        <div class="flex gap-3 pt-2">
            <button type="button" @click="$emit('cancel')"
                class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                Cancel
            </button>
            <button type="submit" :disabled="loading"
                class="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                <span v-if="loading">
                    <i class="fas fa-spinner fa-spin mr-1"></i>
                    {{ isEdit ? 'Saving...' : 'Creating...' }}
                </span>
                <span v-else>{{ isEdit ? 'Save Changes' : 'Create Store' }}</span>
            </button>
        </div>

    </form>
</template>

<script setup>
import { ref, watch } from 'vue';
import { isValidEmail } from '../../utils/helper';

const props = defineProps({
    initialData: {
        type: Object,
        default: null,
    },
    loading: {
        type: Boolean,
        default: false,
    },
    serverError: {
        type: String,
        default: null,
    },
    isEdit: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(['submit', 'cancel']);

const form = ref({
    name: '',
    description: '',
    address: '',
    phone: '',
    email: '',
});

const errors = ref({});

// Populate form when editing
watch(() => props.initialData, (data) => {
    if (data) {
        form.value = {
            name: data.name || '',
            description: data.description || '',
            address: data.address || '',
            phone: data.phone || '',
            email: data.email || '',
        };
    }
}, { immediate: true });

const validate = () => {
    errors.value = {};
    if (!form.value.name.trim()) {
        errors.value.name = 'Store name is required';
    }
    if (form.value.email && isValidEmail(form.value.email)) {
        errors.value.email = 'Invalid email format';
    }
    return Object.keys(errors.value).length === 0;
};

const handleSubmit = () => {
    if (!validate()) return;
    emit('submit', { ...form.value });
};
</script>