<template>
    <span class="text-xs px-2 py-0.5 rounded-full font-medium" :class="badgeClass">
      {{ status }}
    </span>
  </template>
  
  <script setup>
  import { computed } from 'vue';
  
  const props = defineProps({
    status: {
      type: String,
      required: true,
    },
  });
  
  const badgeClass = computed(() => {
    const map = {
      ACTIVE: 'bg-green-100 text-green-700',
      CLOSED: 'bg-gray-100 text-gray-600',
      SUSPEND: 'bg-red-100 text-red-700',
    };
    return map[props.status] || 'bg-gray-100 text-gray-600';
  });
  </script>