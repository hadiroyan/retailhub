<template>
    <div class="w-full">
        <!-- Label -->
        <label v-if="label" class="block text-sm font-medium text-gray-700 mb-1">
            {{ label }}
            <span v-if="required" class="text-red-500">*</span>
        </label>

        <!-- Input field -->
        <input :type="type" :value="modelValue" :placeholder="placeholder" :disabled="disabled" :required="required"
            :class="inputClasses" @input="handleInput" />

        <!-- Help text -->
        <p v-if="helpText && !error" class="mt-1 text-sm text-gray-500">
            {{ helpText }}
        </p>

        <!-- Error message -->
        <p v-if="error" class="mt-1 text-sm text-red-600">
            {{ error }}
        </p>
    </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
    modelValue: {
        type: [String, Number],
        default: '',
    },
    label: {
        type: String,
        default: '',
    },
    type: {
        type: String,
        default: 'text',
    },
    placeholder: {
        type: String,
        default: '',
    },
    error: {
        type: String,
        default: '',
    },
    disabled: {
        type: Boolean,
        default: false,
    },
    required: {
        type: Boolean,
        default: false,
    },
    helpText: {
        type: String,
        default: '',
    },
});

const emit = defineEmits(['update:modelValue']);

const inputClasses = computed(() => {
    const classes = [
        'w-full px-4 py-2 rounded-lg border',
        'transition-colors duration-200',
        'focus:outline-none focus:ring-2 focus:ring-offset-1',
        'disabled:bg-gray-100 disabled:cursor-not-allowed',
    ];

    if (props.error) {
        classes.push('border-red-500 focus:ring-red-500 focus:border-red-500');
    } else {
        classes.push('border-gray-300 focus:ring-blue-500 focus:border-blue-500');
    }

    return classes.join(' ');
});

const handleInput = (event) => {
    emit('update:modelValue', event.target.value);
};
</script>

<style scoped></style>