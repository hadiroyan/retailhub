<template>
    <CustomerLayout>
        <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

            <!-- Header -->
            <div class="mb-6">
                <button @click="router.push({ name: ROUTE_NAMES.CART })"
                    class="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-800 transition-colors mb-4 cursor-pointer">
                    <i class="fas fa-arrow-left"></i>
                    Back to Cart
                </button>
                <h1 class="text-2xl font-bold text-gray-900">Checkout</h1>
                <p class="text-sm text-gray-500 mt-0.5">Review your order and complete purchase</p>
            </div>

            <!-- Empty cart redirect -->
            <div v-if="isEmpty" class="flex flex-col items-center justify-center py-20 text-center">
                <p class="text-sm text-gray-500 mb-4">Your cart is empty</p>
                <button @click="router.push({ name: ROUTE_NAMES.EXPLORE })"
                    class="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors">
                    Explore Products
                </button>
            </div>

            <div v-else class="grid grid-cols-1 lg:grid-cols-3 gap-6">

                <!-- Left: Shipping form -->
                <div class="lg:col-span-2 space-y-4">

                    <!-- Shipping info -->
                    <div class="bg-white rounded-xl border border-gray-200 p-6">
                        <h2 class="text-base font-semibold text-gray-900 mb-4">Shipping Information</h2>
                        <div class="space-y-4">
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">
                                    Full Name <span class="text-red-500">*</span>
                                </label>
                                <input v-model="form.fullName" type="text" :placeholder="userName"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    :class="{ 'border-red-400': errors.fullName }" />
                                <p v-if="errors.fullName" class="text-xs text-red-500 mt-1">{{ errors.fullName }}</p>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">
                                    Email <span class="text-red-500">*</span>
                                </label>
                                <input v-model="form.email" type="email" placeholder="your@email.com"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    :class="{ 'border-red-400': errors.email }" />
                                <p v-if="errors.email" class="text-xs text-red-500 mt-1">{{ errors.email }}</p>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">
                                    Phone Number <span class="text-red-500">*</span>
                                </label>
                                <input v-model="form.phoneNumber" type="text"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    :class="{ 'border-red-400': errors.phoneNumber }" />
                                <p v-if="errors.phoneNumber" class="text-xs text-red-500 mt-1">{{ errors.phoneNumber }}
                                </p>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">
                                    Shipping Address <span class="text-red-500">*</span>
                                </label>
                                <textarea v-model="form.shippingAddress" rows="3"
                                    placeholder="Enter your full shipping address"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                                    :class="{ 'border-red-400': errors.shippingAddress }"></textarea>
                                <p v-if="errors.shippingAddress" class="text-xs text-red-500 mt-1">{{
                                    errors.shippingAddress }}</p>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Notes (optional)</label>
                                <input v-model="form.notes" type="text" placeholder="e.g. Leave at front door"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500" />
                            </div>
                        </div>
                    </div>

                    <!-- Order per store -->
                    <div v-for="storeGroup in cartStores" :key="storeGroup.storeId"
                        class="bg-white rounded-xl border border-gray-200 overflow-hidden">
                        <div class="flex items-center gap-2 px-4 py-3 bg-gray-50 border-b border-gray-200">
                            <i class="fas fa-store text-gray-400 text-sm"></i>
                            <span class="text-sm font-medium text-gray-700">{{ storeGroup.storeName }}</span>
                        </div>
                        <div class="divide-y divide-gray-100">
                            <div v-for="item in storeGroup.items" :key="item.productId"
                                class="flex items-center gap-3 px-4 py-3">
                                <div
                                    class="w-10 h-10 bg-gray-100 rounded-lg overflow-hidden flex items-center justify-center shrink-0">
                                    <img v-if="item.imageUrl" :src="getImageUrl(item.imageUrl)" :alt="item.name"
                                        class="w-full h-full object-cover" />
                                    <i v-else class="fas fa-box text-gray-300 text-sm"></i>
                                </div>
                                <div class="flex-1 min-w-0">
                                    <p class="text-sm font-medium text-gray-900 truncate">{{ item.name }}</p>
                                    <p class="text-xs text-gray-400">{{ item.quantity }} x {{ formatCurrency(item.price)
                                    }}</p>
                                </div>
                                <p class="text-sm font-semibold text-gray-900 shrink-0">
                                    {{ formatCurrency(item.price * item.quantity) }}
                                </p>
                            </div>
                        </div>
                        <div class="flex justify-between px-4 py-3 bg-gray-50 border-t border-gray-200 text-sm">
                            <span class="text-gray-500">Subtotal</span>
                            <span class="font-semibold text-gray-900">{{ formatCurrency(storeGroup.subtotal) }}</span>
                        </div>
                    </div>

                </div>

                <!-- Right: Summary + place order -->
                <div class="lg:col-span-1">
                    <div class="bg-white rounded-xl border border-gray-200 p-5 sticky top-24">
                        <h2 class="text-base font-semibold text-gray-900 mb-4">Order Summary</h2>

                        <div class="space-y-2 mb-4 text-sm">
                            <div v-for="storeGroup in cartStores" :key="storeGroup.storeId"
                                class="flex justify-between">
                                <span class="text-gray-500 truncate max-w-32">{{ storeGroup.storeName }}</span>
                                <span class="font-medium text-gray-900">{{ formatCurrency(storeGroup.subtotal) }}</span>
                            </div>
                        </div>

                        <div class="border-t border-gray-200 pt-4 mb-5">
                            <div class="flex justify-between">
                                <span class="font-semibold text-gray-900">Total</span>
                                <span class="font-bold text-blue-600 text-lg">{{ formatCurrency(totalPrice) }}</span>
                            </div>
                            <p class="text-xs text-gray-400 mt-1">
                                {{ cartStores.length }} order{{ cartStores.length !== 1 ? 's' : '' }} will be created
                            </p>
                        </div>

                        <!-- Error -->
                        <div v-if="serverError"
                            class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600 mb-4">
                            {{ serverError }}
                        </div>

                        <!-- Success -->
                        <div v-if="orderSuccess"
                            class="p-3 bg-green-50 border border-green-200 rounded-lg text-sm text-green-600 mb-4">
                            <i class="fas fa-check-circle mr-1"></i>
                            Orders placed successfully!
                        </div>

                        <button @click="handlePlaceOrder" :disabled="loading || orderSuccess"
                            class="w-full px-4 py-3 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed">
                            <span v-if="loading">
                                <i class="fas fa-spinner fa-spin mr-1"></i>Placing Orders...
                            </span>
                            <span v-else-if="orderSuccess">
                                <i class="fas fa-check mr-1"></i>Orders Placed!
                            </span>
                            <span v-else>Place Order</span>
                        </button>

                        <p class="text-xs text-center text-gray-400 mt-3">
                            By placing order you agree to our terms
                        </p>
                    </div>
                </div>

            </div>
        </div>
    </CustomerLayout>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import { useCartStore } from '../../stores/cartStore';
import { useAuthStore } from '../../stores/auth';
import { ROUTE_NAMES } from '../../utils/constants';
import { formatCurrency, getImageUrl } from '../../utils/helper';

const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();

const { cartStores, totalPrice, isEmpty } = storeToRefs(cartStore);
const { userName, user } = storeToRefs(authStore);

const form = ref({
    fullName: user.value?.fullName || '',
    email: user.value?.email || '',
    phoneNumber: '',
    shippingAddress: '',
    notes: '',
});

const errors = ref({});
const loading = ref(false);
const serverError = ref(null);
const orderSuccess = ref(false);

const validate = () => {
    errors.value = {};
    if (!form.value.fullName.trim()) errors.value.fullName = 'Full name is required';
    if (!form.value.shippingAddress.trim()) errors.value.shippingAddress = 'Shipping address is required';
    if (!form.value.phoneNumber.trim()) errors.value.phoneNumber = 'Phone number is required';
    if (!form.value.email.trim()) errors.value.email = 'Email is required';
    return Object.keys(errors.value).length === 0;
};

const handlePlaceOrder = async () => {
    if (!validate()) return;

    loading.value = true;
    serverError.value = null;

    try {
        // TODO: call POST /api/v1/orders when backend is ready
        // Simulate API call untuk sekarang
        await new Promise(resolve => setTimeout(resolve, 1500));

        orderSuccess.value = true;
        cartStore.clearCart();

        // Redirect ke order history setelah 2 detik
        setTimeout(() => {
            router.push({ name: ROUTE_NAMES.ORDER_HISTORY });
        }, 2000);

    } catch (err) {
        serverError.value = err.response?.data?.message || 'Failed to place order. Please try again.';
    } finally {
        loading.value = false;
    }
};
</script>
