import { defineStore } from "pinia";
import { ref } from "vue";
import { orderService } from "../services/orderService";

export const useOrderStore = defineStore("order", () => {
  // =========================================================================
  // State
  // =========================================================================

  // Customer orders
  const orders = ref([]);
  const totalOrders = ref(0);
  const currentOrder = ref(null);

  // Store orders
  const storeOrders = ref([]);
  const totalStoreOrders = ref(0);

  const loading = ref(false);
  const error = ref(null);

  // =========================================================================
  // Customer actions
  // =========================================================================

  async function createOrder(data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await orderService.createOrder(data);
      orders.value.unshift(response.data);
      totalOrders.value++;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to create order";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchOrders(page = 0, size = 10) {
    loading.value = true;
    error.value = null;
    try {
      const response = await orderService.listOrders({ page, size });
      orders.value = response.data.content;
      totalOrders.value = response.data.totalElements;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load orders";
    } finally {
      loading.value = false;
    }
  }

  async function fetchOrder(id) {
    loading.value = true;
    error.value = null;
    try {
      const response = await orderService.getOrder(id);
      currentOrder.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Order not found";
    } finally {
      loading.value = false;
    }
  }

  async function cancelOrder(id) {
    loading.value = true;
    error.value = null;
    try {
      const response = await orderService.cancelOrder(id);
      // Update order list
      const index = orders.value.findIndex((o) => o.id === id);
      if (index !== -1) orders.value[index] = response.data;
      if (currentOrder.value?.id === id) currentOrder.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to cancel order";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // =========================================================================
  // Store actions
  // =========================================================================

  async function fetchStoreOrders(storeId, params = {}) {
    loading.value = true;
    error.value = null;
    try {
      const response = await orderService.listStoreOrders(storeId, params);
      storeOrders.value = response.data.content;
      totalStoreOrders.value = response.data.totalElements;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load orders";
    } finally {
      loading.value = false;
    }
  }

  async function updateOrderStatus(storeId, orderId, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await orderService.updateOrderStatus(storeId, orderId, data);
      // Update order di list
      const index = storeOrders.value.findIndex((o) => o.id === orderId);
      if (index !== -1) storeOrders.value[index] = response.data;
      if (currentOrder.value?.id === orderId) currentOrder.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to update order status";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // =========================================================================
  // Helpers
  // =========================================================================

  function clearError() {
    error.value = null;
  }

  function clearCurrentOrder() {
    currentOrder.value = null;
  }

  return {
    // State
    orders,
    totalOrders,
    currentOrder,
    storeOrders,
    totalStoreOrders,
    loading,
    error,

    // Customer actions
    createOrder,
    fetchOrders,
    fetchOrder,
    cancelOrder,

    // Store actions
    fetchStoreOrders,
    updateOrderStatus,

    // Helpers
    clearError,
    clearCurrentOrder,
  };
});