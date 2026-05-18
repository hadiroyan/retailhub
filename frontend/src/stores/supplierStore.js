import { defineStore } from "pinia";
import { ref } from "vue";
import { supplierService } from "../services/supplierService";

export const useSupplierStore = defineStore("supplier", () => {
  // =========================================================================
  // State
  // =========================================================================

  // Suppliers
  const suppliers = ref([]);
  const totalSuppliers = ref(0);
  const currentSupplier = ref(null);

  // Purchase Orders
  const purchaseOrders = ref([]);
  const totalPurchaseOrders = ref(0);
  const currentPurchaseOrder = ref(null);

  const loading = ref(false);
  const error = ref(null);

  // =========================================================================
  // Supplier actions
  // =========================================================================

  async function fetchSuppliers(storeId, params = {}) {
    loading.value = true;
    error.value = null;
    try {
      const response = await supplierService.listSuppliers(storeId, params);
      suppliers.value = response.data.content;
      totalSuppliers.value = response.data.totalElements;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load suppliers";
    } finally {
      loading.value = false;
    }
  }

  async function fetchSupplier(storeId, id) {
    loading.value = true;
    error.value = null;
    try {
      const response = await supplierService.getSupplier(storeId, id);
      currentSupplier.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Supplier not found";
    } finally {
      loading.value = false;
    }
  }

  async function createSupplier(storeId, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await supplierService.createSupplier(storeId, data);
      suppliers.value.unshift(response.data);
      totalSuppliers.value++;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to create supplier";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateSupplier(storeId, id, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await supplierService.updateSupplier(storeId, id, data);
      const index = suppliers.value.findIndex((s) => s.id === id);
      if (index !== -1) suppliers.value[index] = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to update supplier";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteSupplier(storeId, id) {
    loading.value = true;
    error.value = null;
    try {
      await supplierService.deleteSupplier(storeId, id);
      suppliers.value = suppliers.value.filter((s) => s.id !== id);
      totalSuppliers.value--;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to delete supplier";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // =========================================================================
  // Purchase Order actions
  // =========================================================================

  async function fetchPurchaseOrders(storeId, params = {}) {
    loading.value = true;
    error.value = null;
    try {
      const response = await supplierService.listPurchaseOrders(storeId, params);
      purchaseOrders.value = response.data.content;
      totalPurchaseOrders.value = response.data.totalElements;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load purchase orders";
    } finally {
      loading.value = false;
    }
  }

  async function fetchPurchaseOrder(storeId, id) {
    loading.value = true;
    error.value = null;
    try {
      const response = await supplierService.getPurchaseOrder(storeId, id);
      currentPurchaseOrder.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Purchase order not found";
    } finally {
      loading.value = false;
    }
  }

  async function createPurchaseOrder(storeId, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await supplierService.createPurchaseOrder(storeId, data);
      purchaseOrders.value.unshift(response.data);
      totalPurchaseOrders.value++;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to create purchase order";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updatePurchaseOrderStatus(storeId, id, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await supplierService.updatePurchaseOrderStatus(storeId, id, data);
      const index = purchaseOrders.value.findIndex((o) => o.id === id);
      if (index !== -1) purchaseOrders.value[index] = response.data;
      if (currentPurchaseOrder.value?.id === id) {
        currentPurchaseOrder.value = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to update purchase order status";
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

  function clearCurrentSupplier() {
    currentSupplier.value = null;
  }

  function clearCurrentPurchaseOrder() {
    currentPurchaseOrder.value = null;
  }

  return {
    // State
    suppliers,
    totalSuppliers,
    currentSupplier,
    purchaseOrders,
    totalPurchaseOrders,
    currentPurchaseOrder,
    loading,
    error,

    // Supplier actions
    fetchSuppliers,
    fetchSupplier,
    createSupplier,
    updateSupplier,
    deleteSupplier,

    // Purchase Order actions
    fetchPurchaseOrders,
    fetchPurchaseOrder,
    createPurchaseOrder,
    updatePurchaseOrderStatus,

    // Helpers
    clearError,
    clearCurrentSupplier,
    clearCurrentPurchaseOrder,
  };
});
