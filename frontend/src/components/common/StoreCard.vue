<template>
    <div
        class="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-md hover:border-blue-200 transition-all duration-200">

        <!-- Header -->
        <div class="flex items-start justify-between mb-3">
            <div class="flex-1 min-w-0">
                <h3 class="font-semibold text-gray-900 truncate">{{ store.name }}</h3>
                <p class="text-xs text-gray-400 mt-0.5">{{ store.slug }}</p>
            </div>
            <StoreStatusBadge :status="store.status" class="ml-2 shrink-0" />
        </div>

        <!-- Description -->
        <p v-if="store.description" class="text-sm text-gray-500 line-clamp-2 mb-4">
            {{ store.description }}
        </p>
        <div v-else class="mb-4"></div>

        <!-- Footer -->
        <div class="flex items-center justify-between pt-3 border-t border-gray-100">
            <div class="flex items-center gap-1 text-xs text-gray-400">
                <i class="fas fa-map-marker-alt"></i>
                <span class="truncate max-w-32">{{ store.address || 'No address' }}</span>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-1">
                <slot name="actions"></slot>
            </div>
        </div>

    </div>
</template>

<script setup>
import StoreStatusBadge from './StoreStatusBadge.vue';

defineProps({
    store: {
        type: Object,
        required: true,
    },
});
</script>