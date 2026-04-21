<template>
    <Teleport to="body">
        <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
            @click.self="$emit('update:modelValue', false)">
            <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">

                <!-- Icon -->
                <div class="flex items-center justify-center w-12 h-12 rounded-full mx-auto mb-4" :class="iconBgClass">
                    <i class="fas fa-toggle-on" :class="iconColorClass"></i>
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
                        class="flex-1 px-4 py-2 text-sm font-medium text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                        :class="confirmBtnClass">
                        <span v-if="loading">
                            <i class="fas fa-spinner fa-spin mr-1"></i> Updating...
                        </span>
                        <span v-else>{{ confirmLabel }}</span>
                    </button>
                </div>

            </div>
        </div>
    </Teleport>
</template>

<script setup>
import { computed } from 'vue';
import { STORE_STATUS } from '../../utils/constants';

const props = defineProps({
    modelValue: {
        type: Boolean,
        required: true,
    },
    targetStatus: {
        type: String,
        required: true, // ACTIVE, CLOSED, SUSPEND
    },
    loading: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(['update:modelValue', 'confirm']);

const title = computed(() => {
    const map = {
        [STORE_STATUS.ACTIVE]: 'Reopen Store',
        [STORE_STATUS.CLOSED]: 'Close Store',
        [STORE_STATUS.SUSPEND]: 'Suspend Store',
    };
    return map[props.targetStatus] || 'Change Status';
});

const description = computed(() => {
    const map = {
        [STORE_STATUS.ACTIVE]: 'This store will be visible to the public again.',
        [STORE_STATUS.CLOSED]: 'This store will be hidden from the public. You can reopen it anytime.',
        [STORE_STATUS.SUSPEND]: 'This store will be suspended. The owner will not be able to manage it.',
    };
    return map[props.targetStatus] || 'Are you sure you want to change this store status?';
});

const confirmLabel = computed(() => {
    const map = {
        [STORE_STATUS.ACTIVE]: 'Reopen',
        [STORE_STATUS.CLOSED]: 'Close Store',
        [STORE_STATUS.SUSPEND]: 'Suspend',
    };
    return map[props.targetStatus] || 'Confirm';
});

const iconBgClass = computed(() => {
    const map = {
        [STORE_STATUS.ACTIVE]: 'bg-green-100',
        [STORE_STATUS.CLOSED]: 'bg-gray-100',
        [STORE_STATUS.SUSPEND]: 'bg-red-100',
    };
    return map[props.targetStatus] || 'bg-gray-100';
});

const iconColorClass = computed(() => {
    const map = {
        [STORE_STATUS.ACTIVE]: 'text-green-600',
        [STORE_STATUS.CLOSED]: 'text-gray-600',
        [STORE_STATUS.SUSPEND]: 'text-red-600',
    };
    return map[props.targetStatus] || 'text-gray-600';
});

const confirmBtnClass = computed(() => {
    const map = {
        [STORE_STATUS.ACTIVE]: 'bg-green-600 hover:bg-green-700',
        [STORE_STATUS.CLOSED]: 'bg-gray-600 hover:bg-gray-700',
        [STORE_STATUS.SUSPEND]: 'bg-red-600 hover:bg-red-700',
    };
    return map[props.targetStatus] || 'bg-blue-600 hover:bg-blue-700';
});

const handleConfirm = () => {
    emit('confirm', props.targetStatus);
};
</script>