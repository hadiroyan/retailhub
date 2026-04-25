<template>
    <CustomerLayout>
        <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

            <!-- Loading -->
            <div v-if="loading" class="flex items-center justify-center py-20">
                <i class="fas fa-spinner fa-spin text-blue-600 text-2xl"></i>
            </div>

            <!-- Not found -->
            <div v-else-if="!product" class="flex flex-col items-center justify-center py-20 text-center">
                <div class="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mb-4">
                    <i class="fas fa-box-open text-gray-300 text-2xl"></i>
                </div>
                <h3 class="font-semibold text-gray-900 mb-1">Product not found</h3>
                <button @click="router.back()" class="mt-3 text-sm text-blue-600 hover:underline">
                    Go back
                </button>
            </div>

            <template v-else>
                <!-- Back -->
                <button @click="router.back()"
                    class="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-800 transition-colors mb-6 cursor-pointer">
                    <i class="fas fa-arrow-left"></i>
                    Back
                </button>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">

                    <!-- Image -->
                    <div class="w-full aspect-square bg-gray-100 rounded-xl flex items-center justify-center">
                        <i class="fas fa-box text-gray-300 text-6xl"></i>
                    </div>

                    <!-- Info -->
                    <div class="flex flex-col">
                        <!-- Store badge -->
                        <button @click="goToStore" class="inline-flex items-center gap-1 
                            text-xs text-gray-500 bg-gray-100 hover:bg-gray-200 hover:text-gray-700
                            px-2 py-0.5 rounded-full mb-3 w-fit transition-colors cursor-pointer">
                            <i class="fas fa-store text-xs"></i>
                            {{ product.store?.name }}
                        </button>

                        <!-- Name -->
                        <h1 class="text-2xl font-bold text-gray-900 mb-2">{{ product.name }}</h1>

                        <!-- Category -->
                        <p v-if="product.category" class="text-xs text-gray-400 mb-3">
                            <i class="fas fa-folder mr-1"></i>{{ product.category.name }}
                        </p>

                        <!-- Price -->
                        <p class="text-2xl font-bold text-gray-900 mb-4">{{ formatCurrency(product.price) }}</p>

                        <!-- Description -->
                        <p v-if="product.description" class="text-sm text-gray-500 mb-6 leading-relaxed">
                            {{ product.description }}
                        </p>

                        <!-- Stock -->
                        <div class="flex items-center gap-1 mb-6">
                            <span class="text-sm text-gray-500">Stock:</span>
                            <span class="text-sm font-medium"
                                :class="product.stockQuantity <= 0 ? 'text-red-600' : 'text-green-600'">
                                {{ product.stockQuantity <= 0 ? 'Out of stock' : `${product.stockQuantity} available` }}
                                    </span>
                        </div>

                        <!-- Add to cart -->
                        <div v-if="product.stockQuantity > 0">
                            <!-- Not authenticated -->
                            <button v-if="!isAuthenticated" @click="router.push({ name: ROUTE_NAMES.LOGIN })"
                                class="w-full px-6 py-3 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-colors">
                                <i class="fas fa-sign-in-alt mr-2"></i>Login to Add to Cart
                            </button>

                            <!-- Authenticated — quantity counter atau add button -->
                            <template v-else>
                                <div v-if="getQuantity() === 0">
                                    <button @click="handleAddToCart"
                                        class="w-full px-6 py-3 text-sm font-medium bg-white text-blue-600 border border-blue-600 hover:bg-blue-600 hover:text-white rounded-xl transition-colors">
                                        <i class="fas fa-cart-plus mr-2"></i>Add to Cart
                                    </button>
                                </div>
                                <div v-else class="flex items-center gap-3">
                                    <button @click="decreaseQuantity"
                                        class="w-10 h-10 flex items-center justify-center bg-white border border-red-500 text-red-500 hover:bg-red-500 hover:text-white rounded-xl transition-colors">
                                        <i class="fas fa-minus text-sm"></i>
                                    </button>
                                    <span class="text-lg font-bold text-gray-900 w-8 text-center">
                                        {{ getQuantity() }}
                                    </span>
                                    <button @click="handleAddToCart"
                                        class="w-10 h-10 flex items-center justify-center bg-white border border-blue-500 text-blue-500 hover:bg-blue-500 hover:text-white rounded-xl transition-colors">
                                        <i class="fas fa-plus text-sm"></i>
                                    </button>
                                    <button @click="router.push({ name: ROUTE_NAMES.CART })"
                                        class="flex-1 h-10 px-4 py-2 text-sm font-medium text-blue-600 border border-blue-600 hover:bg-blue-600 hover:text-white rounded-xl transition-colors">
                                        <i class="fas fa-shopping-cart mr-2"></i>View Cart
                                    </button>
                                </div>
                            </template>
                        </div>

                        <!-- Out of stock -->
                        <button v-else disabled
                            class="w-full px-6 py-3 text-sm font-medium text-gray-400 bg-gray-100 rounded-xl cursor-not-allowed">
                            Out of Stock
                        </button>

                    </div>
                </div>

            </template>
        </div>
    </CustomerLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { storeToRefs } from 'pinia';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import { useCartStore } from '../../stores/cartStore';
import { useAuthStore } from '../../stores/auth';
import { ROUTE_NAMES, API_ENDPOINTS } from '../../utils/constants';
import { formatCurrency } from '../../utils/helper';
import api from '../../services/api';

const router = useRouter();
const route = useRoute();
const cartStore = useCartStore();
const authStore = useAuthStore();
const { isAuthenticated } = storeToRefs(authStore);

const product = ref(null);
const loading = ref(true);

const fetchProduct = async () => {
    loading.value = true;
    try {
        // Fetch the store by slug to get the store ID
        const storeResponse = await api.get(
            API_ENDPOINTS.STORES.BY_SLUG(route.params.storeSlug)
        );
        const store = storeResponse.data.data;

        // Retrieve a product by SKU using the store ID
        const productResponse = await api.get(
            API_ENDPOINTS.PRODUCTS.BY_SKU(store.id, route.params.sku)
        );

        // Add store information to the product
        // because BY_SKU does not include the store
        product.value = {
            ...productResponse.data.data,
            store: {
                id: store.id,
                name: store.name,
                slug: store.slug,
            }
        };
    } catch {
        product.value = null;
    } finally {
        loading.value = false;
    }
};

// Cart helpers — access the store from product.store
const getQuantity = () => {
    if (!product.value?.store?.id) return 0;
    return cartStore.items.find(
        i => i.productId === product.value.id && i.storeId === product.value.store.id
    )?.quantity || 0;
};

const handleAddToCart = () => {
    if (!product.value?.store) return;
    cartStore.addItem(product.value, product.value.store);
};

const decreaseQuantity = () => {
    if (!product.value?.store?.id) return;
    cartStore.updateQuantity(
        product.value.id,
        product.value.store.id,
        getQuantity() - 1
    );
};

const goToStore = () => {
    router.push({
        name: ROUTE_NAMES.STORE_DETAIL,
        params: { slug: product.value.store?.slug },
    });
};

onMounted(() => fetchProduct());
</script>
