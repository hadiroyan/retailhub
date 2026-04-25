<template>
    <CustomerLayout>
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <!-- Header -->
            <div class="mb-6">
                <h1 class="text-2xl font-bold text-gray-900">Cart</h1>
                <p class="text-sm text-gray-500 mt-0.5">
                    {{ totalItems }} item{{ totalItems !== 1 ? 's' : '' }} in your cart
                </p>
            </div>

            <!-- Empty cart -->
            <div v-if="isEmpty" class="flex flex-col items-center justify-center py-20 text-center">
                <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center mb-4">
                    <i class="fas fa-shopping-cart text-blue-400 text-2xl"></i>
                </div>
                <h3 class="font-semibold text-gray-900 mb-1">Your cart is empty</h3>
                <p class="text-sm text-gray-500 mb-4">Explore products and add them to your cart</p>
                <button @click="router.push({ name: ROUTE_NAMES.EXPLORE })"
                    class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                    Explore Products
                </button>
            </div>

            <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">

                <!-- Cart items — grouped by store -->
                <div class="lg:col-span-2 space-y-4">
                    <div v-for="storeGroup in cartStores" :key="storeGroup.storeId"
                        class="bg-white rounded-xl border border-gray-200 overflow-hidden">
                        <!-- Store header -->
                        <div class="flex items-center justify-between px-4 py-3 bg-gray-50 border-b border-gray-200">
                            <button
                                @click="router.push({ name: ROUTE_NAMES.STORE_DETAIL, params: { slug: storeGroup.storeSlug } })"
                                class="flex items-center gap-2 text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors">
                                <i class="fas fa-store text-gray-400"></i>
                                {{ storeGroup.storeName }}
                            </button>
                            <button @click="cartStore.removeStoreItems(storeGroup.storeId)"
                                class="text-xs text-red-500 hover:text-red-600 transition-colors">
                                Remove all
                            </button>
                        </div>

                        <!-- Items -->
                        <div class="divide-y divide-gray-100">
                            <div v-for="item in storeGroup.items" :key="item.productId"
                                class="flex items-center gap-4 px-4 py-3">
                                <!-- Image placeholder -->
                                <div class="w-14 h-14 bg-gray-100 rounded-lg flex items-center justify-center shrink-0">
                                    <i class="fas fa-box text-gray-300"></i>
                                </div>

                                <!-- Info -->
                                <div class="flex-1 min-w-0">
                                    <p class="text-sm font-medium text-gray-900 truncate">{{ item.name }}</p>
                                    <p class="text-xs text-gray-400 font-mono">{{ item.sku }}</p>
                                    <p class="text-sm font-medium text-gray-900 mt-0.5">
                                        {{ formatCurrency(item.price) }}
                                    </p>
                                </div>

                                <!-- Quantity counter -->
                                <div class="flex items-center gap-2 shrink-0">
                                    <button
                                        @click="cartStore.updateQuantity(item.productId, item.storeId, item.quantity - 1)"
                                        class="w-7 h-7 flex items-center justify-center bg-white border border-gray-300 hover:border-red-400 hover:text-red-500 rounded-lg text-xs transition-colors">
                                        <i class="fas fa-minus"></i>
                                    </button>
                                    <span class="text-sm font-semibold text-gray-900 w-6 text-center">
                                        {{ item.quantity }}
                                    </span>
                                    <button
                                        @click="cartStore.updateQuantity(item.productId, item.storeId, item.quantity + 1)"
                                        class="w-7 h-7 flex items-center justify-center bg-white border border-gray-300 hover:border-blue-400 hover:text-blue-600 rounded-lg text-xs transition-colors">
                                        <i class="fas fa-plus"></i>
                                    </button>
                                </div>

                                <!-- Subtotal + remove -->
                                <div class="text-right shrink-0">
                                    <p class="text-sm font-semibold text-gray-900">
                                        {{ formatCurrency(item.price * item.quantity) }}
                                    </p>
                                    <button @click="cartStore.removeItem(item.productId, item.storeId)"
                                        class="text-xs text-red-400 hover:text-red-600 transition-colors mt-0.5">
                                        Remove
                                    </button>
                                </div>
                            </div>
                        </div>

                        <!-- Store subtotal -->
                        <div class="flex items-center justify-between px-4 py-3 bg-gray-50 border-t border-gray-200">
                            <span class="text-sm text-gray-500">Subtotal ({{ storeGroup.storeName }})</span>
                            <span class="text-sm font-semibold text-gray-900">
                                {{ formatCurrency(storeGroup.subtotal) }}
                            </span>
                        </div>
                    </div>
                </div>

                <!-- Order summary -->
                <div class="lg:col-span-1">
                    <div class="bg-white rounded-xl border border-gray-200 p-5 sticky top-24">
                        <h2 class="text-base font-semibold text-gray-900 mb-4">Order Summary</h2>

                        <div class="space-y-2 mb-4">
                            <div v-for="storeGroup in cartStores" :key="storeGroup.storeId"
                                class="flex items-center justify-between text-sm">
                                <span class="text-gray-500 truncate max-w-32">{{ storeGroup.storeName }}</span>
                                <span class="text-gray-700 font-medium">{{ formatCurrency(storeGroup.subtotal) }}</span>
                            </div>
                        </div>

                        <div class="border-t border-gray-200 pt-4 mb-5">
                            <div class="flex items-center justify-between">
                                <span class="font-semibold text-gray-900">Total</span>
                                <span class="font-bold text-gray-900 text-lg">{{ formatCurrency(totalPrice) }}</span>
                            </div>
                            <p class="text-xs text-gray-400 mt-1">
                                {{ cartStores.length }} store{{ cartStores.length !== 1 ? 's' : '' }} ·
                                {{ totalItems }} item{{ totalItems !== 1 ? 's' : '' }}
                            </p>
                        </div>

                        <!-- Checkout button — auth required -->
                        <button v-if="isAuthenticated" @click="router.push({ name: ROUTE_NAMES.CHECKOUT })"
                            class="w-full px-4 py-3 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-colors cursor-pointer">
                            Proceed to Checkout
                        </button>
                        <div v-else class="space-y-2">
                            <button @click="router.push({ name: ROUTE_NAMES.LOGIN })"
                                class="w-full px-4 py-3 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-colors cursor-pointer">
                                Login to Checkout
                            </button>
                            <p class="text-xs text-center text-gray-400">
                                You need to login to place an order
                            </p>
                        </div>

                        <!-- Clear cart -->
                        <button @click="cartStore.clearCart()" class="w-full mt-3 px-4 py-2 text-sm border border-red-500 text-red-500 
                            hover:bg-red-600 hover:text-white rounded-xl transition-colors cursor-pointer">
                            Clear Cart
                        </button>

                    </div>
                </div>
            </div>

        </div>
    </CustomerLayout>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import { useCartStore } from '../../stores/cartStore';
import { useAuthStore } from '../../stores/auth';
import { ROUTE_NAMES } from '../../utils/constants';
import { formatCurrency } from '../../utils/helper';

const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();

const { items, totalItems, totalPrice, isEmpty, cartStores } = storeToRefs(cartStore);
const { isAuthenticated } = storeToRefs(authStore);
</script>
