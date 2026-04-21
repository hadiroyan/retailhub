<template>
    <Teleport to="body">
        <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
            @click.self="$emit('update:modelValue', false)">
            <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">

                <!-- Icon -->
                <div class="flex items-center justify-center w-12 h-12 rounded-full bg-red-100 mx-auto mb-4">
                    <i class="fas fa-trash text-red-600"></i>
                </div>

                <!-- Text -->
                <h3 class="text-lg font-semibold text-gray-900 text-center mb-1">{{ title }}</h3>
                <p class="text-sm text-gray-500 text-center mb-6">{{ description }}</p>

                <!-- Actions -->
                <div class="flex gap-3">
                    <button @click="$emit('update:modelValue', false)"
                        class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                        Cancel
                    </button>
                    <button @click="handleConfirm" :disabled="loading"
                        class="flex-1 px-4 py-2 text-sm font-medium text-white bg-red-600 hover:bg-red-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                        <span v-if="loading">
                            <i class="fas fa-spinner fa-spin mr-1"></i> Deleting...
                        </span>
                        <span v-else>Delete</span>
                    </button>
                </div>

            </div>
        </div>
    </Teleport>
</template>

<script setup>
defineProps({
    modelValue: {
        type: Boolean,
        required: true,
    },
    title: {
        type: String,
        default: 'Delete Confirmation',
    },
    description: {
        type: String,
        default: 'Are you sure you want to delete this item? This action cannot be undone.',
    },
    loading: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(['update:modelValue', 'confirm']);

const handleConfirm = () => {
    emit('confirm');
};
</script>