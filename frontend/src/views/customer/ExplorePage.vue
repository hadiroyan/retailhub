<template>
    <CustomerLayout>
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

            <!-- Header -->
            <div class="mb-6">
                <h1 class="text-2xl font-bold text-gray-900">Explore Products</h1>
                <p class="text-sm text-gray-500 mt-0.5">Discover products from our stores</p>
            </div>

            <!-- Filters -->
            <div class="flex flex-col sm:flex-row gap-3 mb-6">
                <input v-model="searchName" type="text" placeholder="Search products..."
                    class="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    @input="onSearchInput" />
                <select v-model="sortByPrice"
                    class="px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    @change="fetchWithFilters">
                    <option value="">Default Sort</option>
                    <option value="asc">Price: Low to High</option>
                    <option value="desc">Price: High to Low</option>
                </select>
            </div>

            <!-- Loading -->
            <div v-if="loading" class="flex items-center justify-center py-20">
                <i class="fas fa-spinner fa-spin text-blue-600 text-2xl"></i>
            </div>

            <!-- Error -->
            <div v-else-if="error" class="p-4 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                {{ error }}
            </div>

            <!-- Empty -->
            <div v-else-if="products.length === 0" class="flex flex-col items-center justify-center py-20 text-center">
                <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                    <i class="fas fa-search text-blue-400 text-2xl"></i>
                </div>
                <h3 class="font-semibold text-gray-900 mb-1">No products found</h3>
                <p class="text-sm text-gray-500">Try adjusting your search or filters</p>
            </div>

            <!-- Product grid -->
            <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                <div v-for="product in products" :key="product.id" @click="goToProduct(product)"
                    class="bg-white rounded-xl border border-gray-200 p-4 hover:shadow-md hover:border-gray-300 transition-all duration-200 cursor-pointer">
                    <!-- Product image placeholder -->
                    <div
                        class="w-full h-40 bg-gray-100 rounded-lg mb-3 overflow-hidden flex items-center justify-center">
                        <img v-if="product.imageUrls?.length > 0" :src="getImageUrl(product.imageUrls[0])"
                            :alt="product.name" class="w-full h-full object-cover" />
                        <i v-else class="fas fa-box text-gray-300 text-3xl"></i>
                    </div>

                    <!-- Store badge -->
                    <div @click.stop="goToStore(product.store)"
                        class="inline-flex items-center gap-1 text-xs text-gray-500 rounded-full mb-2 hover:text-gray-800 transition-colors cursor-pointer">
                        <i class="fas fa-store text-xs"></i>
                        {{ product.store?.name }}
                    </div>

                    <!-- Product info -->
                    <h3 class="font-medium text-gray-900 text-sm mb-1 line-clamp-2">{{ product.name }}</h3>
                    <p v-if="product.description" class="text-xs text-gray-400 line-clamp-1 mb-2">
                        {{ product.description }}
                    </p>

                    <!-- Price + Cart -->
                    <div class="flex items-center justify-between mt-auto pt-2 border-t border-gray-100">
                        <span class="font-semibold text-gray-900 text-sm">{{ formatCurrency(product.price) }}</span>
                        <!-- Not in cart yet -->
                        <button v-if="!isAuthenticated || getQuantity(product.id, product.store?.id) === 0"
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
                            <button
                                @click="cartStore.updateQuantity(product.id, product.store?.id, getQuantity(product.id, product.store?.id) - 1)"
                                class="w-6 h-6 flex items-center justify-center 
                                bg-white text-red-500 border border-red-500 hover:bg-red-500 hover:text-white 
                                rounded text-xs font-bold transition-colors cursor-pointer">
                                <i class="fas fa-minus text-xs"></i>
                            </button>
                            <span class="text-xs font-semibold text-gray-800 w-4 text-center">
                                {{ getQuantity(product.id, product.store?.id) }}
                            </span>
                            <button @click="cartStore.addItem(product, product.store)" class="w-6 h-6 flex items-center justify-center 
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
                class="flex items-center justify-between mt-8 pt-4 border-t border-gray-200">
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

        </div>
    </CustomerLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import { useCartStore } from '../../stores/cartStore';
import { ROUTE_NAMES, API_ENDPOINTS } from '../../utils/constants';
import { formatCurrency, debounce, getImageUrl } from '../../utils/helper';
import api from '../../services/api';
import { useAuthStore } from '../../stores/auth';
import { storeToRefs } from 'pinia';

const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();

const { isAuthenticated } = storeToRefs(authStore);
const { getQuantity } = cartStore;

// State
const products = ref([]);
const totalProducts = ref(0);
const loading = ref(false);
const error = ref(null);
const currentPage = ref(0);
const pageSize = ref(20);
const searchName = ref('');
const sortByPrice = ref('');

const fetchWithFilters = async (page = 0) => {
    loading.value = true;
    error.value = null;
    currentPage.value = page;
    try {
        const params = {
            page,
            size: pageSize.value,
            name: searchName.value || undefined,
            sortByPrice: sortByPrice.value || undefined,
        };
        const response = await api.get(API_ENDPOINTS.EXPLORE.PRODUCTS, { params });
        products.value = response.data.data.content;
        totalProducts.value = response.data.data.totalElements;
    } catch (err) {
        error.value = err.response?.data?.message || 'Failed to load products';
    } finally {
        loading.value = false;
    }
};

const onSearchInput = debounce(() => fetchWithFilters(0), 400);

const changePage = (page) => fetchWithFilters(page);

const goToProduct = (product) => {
    router.push({
        name: ROUTE_NAMES.PRODUCT_DETAIL,
        params: { storeSlug: product.store?.slug, sku: product.sku },
    });
};

const goToStore = (store) => {
    router.push({
        name: ROUTE_NAMES.STORE_DETAIL,
        params: { slug: store.slug },
    });
};

const handleAddToCart = (product) => {
    cartStore.addItem(product, product.store);
};

onMounted(() => fetchWithFilters());
</script>
