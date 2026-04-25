<template>
    <CustomerLayout>
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

            <!-- Loading -->
            <div v-if="loadingStore" class="flex items-center justify-center py-20">
                <i class="fas fa-spinner fa-spin text-blue-600 text-2xl"></i>
            </div>

            <template v-else-if="store">

                <!-- Store header -->
                <div class="bg-white rounded-xl border border-gray-200 p-6 mb-6">
                    <div class="flex items-start justify-between">
                        <div>
                            <div class="flex items-center gap-3 mb-2">
                                <h1 class="text-2xl font-bold text-gray-900">{{ store.name }}</h1>
                                <StoreStatusBadge :status="store.status" />
                            </div>
                            <p v-if="store.description" class="text-sm text-gray-500 mb-3">{{ store.description }}</p>
                            <div class="flex flex-wrap gap-4 text-xs text-gray-400">
                                <span v-if="store.address"><i class="fas fa-map-marker-alt mr-1"></i>{{ store.address
                                    }}</span>
                                <span v-if="store.phone"><i class="fas fa-phone mr-1"></i>{{ store.phone }}</span>
                                <span v-if="store.email"><i class="fas fa-envelope mr-1"></i>{{ store.email }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Products section -->
                <div class="mb-4 flex items-center justify-between">
                    <h2 class="text-lg font-semibold text-gray-900">Products</h2>
                    <input v-model="searchName" type="text" placeholder="Search products..."
                        class="px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 w-64"
                        @input="onSearchInput" />
                </div>

                <!-- Loading products -->
                <div v-if="loadingProducts" class="flex items-center justify-center py-10">
                    <i class="fas fa-spinner fa-spin text-blue-600 text-xl"></i>
                </div>

                <!-- Empty products -->
                <div v-else-if="products.length === 0"
                    class="flex flex-col items-center justify-center py-16 text-center">
                    <div class="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center mb-3">
                        <i class="fas fa-box text-gray-300 text-xl"></i>
                    </div>
                    <p class="text-sm text-gray-500">No products found in this store</p>
                </div>

                <!-- Product grid -->
                <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    <div v-for="product in products" :key="product.id" @click="goToProduct(product)"
                        class="bg-white rounded-xl border border-gray-200 p-4 hover:shadow-md hover:border-blue-200 transition-all duration-200 cursor-pointer">
                        <!-- Image placeholder -->
                        <div class="w-full h-40 bg-gray-100 rounded-lg mb-3 flex items-center justify-center">
                            <i class="fas fa-box text-gray-300 text-3xl"></i>
                        </div>

                        <h3 class="font-medium text-gray-900 text-sm mb-1 line-clamp-2">{{ product.name }}</h3>
                        <p v-if="product.description" class="text-xs text-gray-400 line-clamp-1 mb-2">
                            {{ product.description }}
                        </p>

                        <div class="flex items-center justify-between mt-auto pt-2 border-t border-gray-100">
                            <span class="font-semibold text-gray-900 text-sm">{{ formatCurrency(product.price) }}</span>
                            <span>{{ getQuantity(product.id, store.value?.id) }}</span>
                            <!-- Not in cart yet -->
                            <button v-if="!isAuthenticated || getQuantity(product.id, store.value?.id) === 0"
                                @click.stop="handleAddToCart(product)" :disabled="!isAuthenticated"
                                class="flex items-center gap-1 px-3 py-1.5 text-xs font-medium rounded-lg transition-colors"
                                :class="!isAuthenticated
                                    ? 'bg-gray-100 text-gray-400'
                                    : 'bg-white text-blue-600 border border-blue-600 hover:bg-blue-600 hover:text-white cursor-pointer'"
                                :title="!isAuthenticated ? 'Login to add to cart' : ''">
                                <i class="fas fa-cart-plus"></i>
                                {{ !isAuthenticated ? 'Login' : 'Add' }}
                            </button>

                            <!-- Already in the cart — quantity counter -->
                            <div v-else class="flex items-center gap-1" @click.stop>
                                <button @click="decreaseQuantity(product)""
                                    class=" w-6 h-6 flex items-center justify-center bg-white text-red-500 border
                                    border-red-500 hover:bg-red-500 hover:text-white rounded text-xs font-bold
                                    transition-colors cursor-pointer">
                                    <i class="fas fa-minus text-xs"></i>
                                </button>
                                <span class="text-xs font-semibold text-gray-800 w-4 text-center">
                                    {{ getQuantity(product.id) }}
                                </span>
                                <button @click="increaseQuantity(product)" class="w-6 h-6 flex items-center justify-center 
                                bg-white text-blue-500 border border-blue-500 hover:bg-blue-500 hover:text-white 
                                rounded text-xs font-bold transition-colors cursor-pointer">
                                    <i class="fas fa-plus text-xs"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Pagination -->
                <div v-if="totalProducts > pageSize"
                    class="flex items-center justify-between mt-6 pt-4 border-t border-gray-200">
                    <p class="text-sm text-gray-500">Showing {{ products.length }} of {{ totalProducts }} products</p>
                    <div class="flex gap-2">
                        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0"
                            class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                            <i class="fas fa-chevron-left"></i>
                        </button>
                        <button @click="changePage(currentPage + 1)"
                            :disabled="(currentPage + 1) * pageSize >= totalProducts"
                            class="px-3 py-1.5 text-sm text-gray-600 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed">
                            <i class="fas fa-chevron-right"></i>
                        </button>
                    </div>
                </div>

            </template>

            <!-- Store not found -->
            <div v-else class="flex flex-col items-center justify-center py-20 text-center">
                <div class="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mb-4">
                    <i class="fas fa-store-slash text-gray-300 text-2xl"></i>
                </div>
                <h3 class="font-semibold text-gray-900 mb-1">Store not found</h3>
                <button @click="router.push({ name: ROUTE_NAMES.EXPLORE })"
                    class="mt-3 text-sm text-blue-600 hover:underline">
                    Back to Explore
                </button>
            </div>

        </div>
    </CustomerLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import StoreStatusBadge from '../../components/common/StoreStatusBadge.vue';
import { useCartStore } from '../../stores/cartStore';
import { ROUTE_NAMES, API_ENDPOINTS } from '../../utils/constants';
import { formatCurrency, debounce } from '../../utils/helper';
import api from '../../services/api';
import { useAuthStore } from '../../stores/auth';
import { storeToRefs } from 'pinia';

const router = useRouter();
const route = useRoute();
const cartStore = useCartStore();
const authStore = useAuthStore();

const { isAuthenticated } = storeToRefs(authStore);

const store = ref(null);
const products = ref([]);
const totalProducts = ref(0);
const loadingStore = ref(true);
const loadingProducts = ref(false);
const currentPage = ref(0);
const pageSize = ref(20);
const searchName = ref('');

const getQuantity = (productId) => {
    if (!store.value?.id) return 0;
    return cartStore.items.find(
        i => i.productId === productId && i.storeId === store.value.id
    )?.quantity || 0;
};

const decreaseQuantity = (product) => {
    if (!store.value?.id) return;
    cartStore.updateQuantity(product.id, store.value.id, getQuantity(product.id) - 1);
};

const increaseQuantity = (product) => {
    if (!store.value) return;
    cartStore.addItem(product, store.value);
};

const fetchStore = async () => {
    loadingStore.value = true;
    try {
        const response = await api.get(API_ENDPOINTS.STORES.BY_SLUG(route.params.slug));
        store.value = response.data.data;
    } catch {
        store.value = null;
    } finally {
        loadingStore.value = false;
    }
};

const fetchProducts = async (page = 0) => {
    if (!store.value) return;
    loadingProducts.value = true;
    currentPage.value = page;
    try {
        const response = await api.get(API_ENDPOINTS.PRODUCTS.BASE(store.value.id), {
            params: {
                page,
                size: pageSize.value,
                name: searchName.value || undefined,
            },
        });
        products.value = response.data.data.content;
        totalProducts.value = response.data.data.totalElements;
    } catch {
        products.value = [];
    } finally {
        loadingProducts.value = false;
    }
};

const onSearchInput = debounce(() => fetchProducts(0), 400);
const changePage = (page) => fetchProducts(page);

const goToProduct = (product) => {
    router.push({
        name: ROUTE_NAMES.PRODUCT_DETAIL,
        params: { storeSlug: route.params.slug, sku: product.sku },
    });
};

const handleAddToCart = (product) => {
    cartStore.addItem(product, store.value);
};

onMounted(async () => {
    await fetchStore();
    await fetchProducts();
});
</script>
