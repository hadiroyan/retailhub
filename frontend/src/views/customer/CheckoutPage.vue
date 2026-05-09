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

                    <!-- Incomplete profile warning -->
                    <div v-if="!isProfileComplete"
                        class="flex items-start gap-3 p-4 bg-yellow-50 border border-yellow-200 rounded-xl">
                        <i class="fas fa-exclamation-triangle text-yellow-500 mt-0.5"></i>
                        <div class="flex-1">
                            <p class="text-sm font-medium text-yellow-800">Profile incomplete</p>
                            <p class="text-xs text-yellow-600 mt-0.5">
                                Please add your phone number and address to proceed with checkout.
                            </p>
                        </div>
                        <button @click="router.push({ name: ROUTE_NAMES.PROFILE })"
                            class="text-xs font-medium text-yellow-700 hover:text-yellow-800 underline shrink-0 cursor-pointer">
                            Update Profile
                        </button>
                    </div>

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
                                    Phone <span class="text-red-500">*</span>
                                </label>
                                <input v-model="form.phone" type="text" placeholder="e.g. 08123456789"
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                    :class="{ 'border-red-400': errors.phone }" />
                                <p v-if="errors.phone" class="text-xs text-red-500 mt-1">{{ errors.phone }}</p>
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
                                    <p class="text-xs text-gray-400">
                                        {{ item.quantity }} x {{ formatCurrency(item.price) }}
                                    </p>
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
                                <span class="font-bold text-gray-900 text-lg">{{ formatCurrency(totalPrice) }}</span>
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
                            <i class="fas fa-check-circle mr-1"></i>Orders placed successfully!
                        </div>

                        <button @click="handlePlaceOrder" :disabled="loading || orderSuccess || !isProfileComplete"
                            class="w-full px-4 py-3 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                            :title="!isProfileComplete ? 'Please complete your profile first' : ''">
                            <span v-if="loading">
                                <i class="fas fa-spinner animate-spin mr-1"></i>Placing Orders...
                            </span>
                            <span v-else-if="orderSuccess">
                                <i class="fas fa-check mr-1"></i>Orders Placed!
                            </span>
                            <span v-else>Place Order</span>
                        </button>

                        <!-- Profile incomplete hint -->
                        <p v-if="!isProfileComplete" class="text-xs text-center text-red-600 mt-2">
                            <i class="fas fa-exclamation-triangle mr-1"></i>
                            Complete your profile to proceed
                        </p>

                        <p v-else class="text-xs text-center text-gray-400 mt-3">
                            By placing order you agree to our terms
                        </p>
                    </div>
                </div>

            </div>
        </div>

        <!-- Profile incomplete modal -->
        <Teleport to="body">
            <div v-if="showProfileModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
                <div class="bg-white rounded-xl shadow-xl w-full max-w-sm p-6 text-center">
                    <div class="w-14 h-14 rounded-full bg-yellow-100 flex items-center justify-center mx-auto mb-4">
                        <i class="fas fa-user-edit text-yellow-600 text-xl"></i>
                    </div>
                    <h3 class="text-lg font-semibold text-gray-900 mb-2">Profile Incomplete</h3>
                    <p class="text-sm text-gray-500 mb-6">
                        You need to add your <strong>phone number</strong> and <strong>address</strong>
                        before placing an order.
                    </p>
                    <div class="flex gap-3">
                        <button @click="showProfileModal = false"
                            class="flex-1 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors">
                            Later
                        </button>
                        <button @click="router.push({ name: ROUTE_NAMES.PROFILE })"
                            class="flex-1 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors">
                            Update Profile
                        </button>
                    </div>
                </div>
            </div>
        </Teleport>

    </CustomerLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import CustomerLayout from '../../layouts/CustomerLayout.vue';
import { useCartStore } from '../../stores/cartStore';
import { useAuthStore } from '../../stores/auth';
import { useOrderStore } from '../../stores/orderStore';
import { ROUTE_NAMES } from '../../utils/constants';
import { formatCurrency, getImageUrl } from '../../utils/helper';

const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();
const orderStore = useOrderStore();

const { cartStores, totalPrice, isEmpty } = storeToRefs(cartStore);
const { userName, user } = storeToRefs(authStore);

// Check whether the profile is complete or not
const isProfileComplete = computed(() =>
    !!(user.value?.phone && user.value?.address)
);

// Display a message when the page loads if the profile is incomplete
const showProfileModal = ref(false);

onMounted(() => {
    if (!isProfileComplete.value) {
        showProfileModal.value = true;
    }

    // Pre-fill form dari user data
    form.value = {
        fullName: user.value?.fullName || '',
        email: user.value?.email || '',
        phone: user.value?.phone || '',
        shippingAddress: user.value?.address || '',
        notes: '',
    };
});

const form = ref({
    fullName: '',
    email: '',
    phone: '',
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
    if (!form.value.email.trim()) errors.value.email = 'Email is required';
    if (!form.value.phone.trim()) errors.value.phone = 'Phone is required';
    if (!form.value.shippingAddress.trim()) errors.value.shippingAddress = 'Shipping address is required';
    return Object.keys(errors.value).length === 0;
};

const handlePlaceOrder = async () => {
    if (!isProfileComplete.value) {
        showProfileModal.value = true;
        return;
    }
    if (!validate()) return;

    loading.value = true;
    serverError.value = null;

    try {
        // Loop cartStores — create one order per store
        for (const storeGroup of cartStores.value) {
            await orderStore.createOrder({
                storeId: storeGroup.storeId,
                recipientName: form.value.fullName,
                phone: form.value.phone,
                shippingAddress: form.value.shippingAddress,
                notes: form.value.notes || undefined,
                items: storeGroup.items.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity,
                })),
            });
        }

        orderSuccess.value = true;
        cartStore.clearCart();
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
