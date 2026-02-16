<template>
    <div :class="cardClasses">
        <slot></slot>
    </div>
</template>
<script setup>
import { computed } from 'vue';

const props = defineProps({
    padding: {
        type: String,
        default: 'md',
        validator: (value) => ['none', 'sm', 'md', 'lg'].includes(value),
    },
    shadow: {
        type: String,
        default: 'md',
        validator: (value) => ['none', 'sm', 'md', 'lg', 'xl'].includes(value),
    },
    rounded: {
        type: String,
        default: 'lg',
        validator: (value) => ['none', 'sm', 'md', 'lg', 'xl'].includes(value),
    },
    bordered: {
        type: Boolean,
        default: true,
    },
});

const cardClasses = computed(() => {
    const classes = ['bg-white'];

    // Padding
    const paddingClasses = {
        none: '',
        sm: 'p-4',
        md: 'p-6',
        lg: 'p-8',
    };

    // Shadow
    const shadowClasses = {
        none: '',
        sm: 'shadow-sm',
        md: 'shadow-md',
        lg: 'shadow-lg',
        xl: 'shadow-xl',
    };

    // Rounded
    const roundedClasses = {
        none: '',
        sm: 'rounded-sm',
        md: 'rounded-md',
        lg: 'rounded-lg',
        xl: 'rounded-xl',
    };

    // Border
    if (props.bordered) {
        classes.push('border border-gray-200');
    }

    classes.push(paddingClasses[props.padding]);
    classes.push(shadowClasses[props.shadow]);
    classes.push(roundedClasses[props.rounded]);

    return classes.join(' ');
});
</script>

<style scoped></style>