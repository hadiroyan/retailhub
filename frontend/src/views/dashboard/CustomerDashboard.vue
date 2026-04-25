<template> </template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ROUTE_NAMES } from '../../utils/constants';

const router = useRouter();

// Redirect directly to the Explore Page
onMounted(() => {
    router.replace({ name: ROUTE_NAMES.EXPLORE });
});
</script>

<style scoped></style>