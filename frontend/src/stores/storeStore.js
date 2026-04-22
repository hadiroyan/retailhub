import { defineStore } from "pinia";
import { ref } from "vue";
import { storeService } from "../services/storeService";

export const useStoreStore = defineStore("store", () => {
  // =========================================================================
  // State
  // =========================================================================

  // Store list (OWNER: my stores, SUPER_ADMIN: all stores)
  const stores = ref([]);
  const totalStores = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);

  const activeStore = ref(null);

  // Employee
  const employees = ref([]);
  const totalEmployees = ref(0);

  // Category
  const categories = ref([]);
  const totalCategories = ref(0);

  // Product
  const products = ref([]);
  const totalProducts = ref(0);
  const lowStockProducts = ref(0);

  // Loading & error state
  const loading = ref(false);
  const error = ref(null);

  // =========================================================================
  // Store actions
  // =========================================================================

  async function fetchStores(page = 0, size = 10) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.listStores({ page, size });
      stores.value = response.data.content;
      totalStores.value = response.data.totalElements;
      currentPage.value = page;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load stores";
    } finally {
      loading.value = false;
    }
  }

  async function fetchStoreBySlug(slug) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.getStoreBySlug(slug);
      activeStore.value = response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Store not found";
    } finally {
      loading.value = false;
    }
  }

  async function createStore(data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.createStore(data);
      stores.value.unshift(response.data);
      totalStores.value++;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to create store";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateStore(id, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.updateStore(id, data);
      const index = stores.value.findIndex((s) => s.id === id);
      if (index !== -1) stores.value[index] = response.data;
      if (activeStore.value?.id === id) activeStore.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to update store";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteStore(id) {
    loading.value = true;
    error.value = null;
    try {
      await storeService.deleteStore(id);
      stores.value = stores.value.filter((s) => s.id !== id);
      totalStores.value--;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to delete store";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateStoreStatus(id, status) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.updateStoreStatus(id, status);
      const index = stores.value.findIndex((s) => s.id === id);
      if (index !== -1) stores.value[index] = response.data;
      if (activeStore.value?.id === id) activeStore.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to update status";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // =========================================================================
  // Employee actions
  // =========================================================================

  async function fetchEmployees(storeId, page = 0, size = 10) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.listEmployees(storeId, {
        page,
        size,
      });
      employees.value = response.data.content;
      totalEmployees.value = response.data.totalElements;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load employees";
    } finally {
      loading.value = false;
    }
  }

  async function createEmployee(storeId, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.createEmployee(storeId, data);
      employees.value.unshift(response.data);
      totalEmployees.value++;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to create employee";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateEmployeeRole(storeId, userId, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.updateEmployeeRole(
        storeId,
        userId,
        data
      );
      const index = employees.value.findIndex((e) => e.id === userId);
      if (index !== -1) employees.value[index] = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to update role";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function removeEmployee(storeId, userId) {
    loading.value = true;
    error.value = null;
    try {
      await storeService.removeEmployee(storeId, userId);
      employees.value = employees.value.filter((e) => e.id !== userId);
      totalEmployees.value--;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to remove employee";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // =========================================================================
  // Category actions
  // =========================================================================

  async function fetchCategories(storeId, page = 0, size = 10) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.listCategories(storeId, {
        page,
        size,
      });
      categories.value = response.data.content;
      totalCategories.value = response.data.totalElements;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load categories";
    } finally {
      loading.value = false;
    }
  }

  async function createCategory(storeId, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.createCategory(storeId, data);
      categories.value.unshift(response.data);
      totalCategories.value++;
      return response.data;
    } catch (err) {
      error.value =
        err.response?.data?.message || "Failed to create category";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateCategory(storeId, id, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.updateCategory(storeId, id, data);
      const index = categories.value.findIndex((c) => c.id === id);
      if (index !== -1) categories.value[index] = response.data;
      return response.data;
    } catch (err) {
      error.value =
        err.response?.data?.message || "Failed to update category";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteCategory(storeId, id) {
    loading.value = true;
    error.value = null;
    try {
      await storeService.deleteCategory(storeId, id);
      categories.value = categories.value.filter((c) => c.id !== id);
      totalCategories.value--;
    } catch (err) {
      error.value =
        err.response?.data?.message || "Failed to delete category";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // =========================================================================
  // Product actions
  // =========================================================================

  async function fetchProducts(storeId, params = {}) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.listProducts(storeId, params);
      products.value = response.data.content;
      totalProducts.value = response.data.totalElements;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load products";
    } finally {
      loading.value = false;
    }
  }

  async function createProduct(storeId, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.createProduct(storeId, data);
      products.value.unshift(response.data);
      totalProducts.value++;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to create product";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateProduct(storeId, id, data) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.updateProduct(storeId, id, data);
      const index = products.value.findIndex((p) => p.id === id);
      if (index !== -1) products.value[index] = response.data;
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to update product";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteProduct(storeId, id) {
    loading.value = true;
    error.value = null;
    try {
      await storeService.deleteProduct(storeId, id);
      products.value = products.value.filter((p) => p.id !== id);
      totalProducts.value--;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to delete product";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchProductDetail(storeId, sku) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.getProductDetail(storeId, sku);
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load product detail";
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchInternalProducts(storeId, params = {}) {
    loading.value = true;
    error.value = null;
    try {
      const response = await storeService.getInternalProducts(storeId, params);
      products.value = response.data.content;
      totalProducts.value = response.data.totalElements;

      lowStockProducts.value = 0;
      for (let index = 0; index < products.value.length; index++) {
        const element = products.value[index];
        if (element.stockQuantity < element.minStockLevel) {
          lowStockProducts.value++;
        }
      }
    } catch (err) {
      error.value = err.response?.data?.message || "Failed to load products";
    } finally {
      loading.value = false;
    }
  }

  // =========================================================================
  // Helpers
  // =========================================================================

  function setActiveStore(store) {
    activeStore.value = store;
  }

  function clearError() {
    error.value = null;
  }

  return {
    // State
    stores,
    totalStores,
    currentPage,
    pageSize,
    activeStore,
    employees,
    totalEmployees,
    categories,
    totalCategories,
    products,
    totalProducts,
    lowStockProducts,
    loading,
    error,

    // Store actions
    fetchStores,
    fetchStoreBySlug,
    createStore,
    updateStore,
    deleteStore,
    updateStoreStatus,

    // Employee actions
    fetchEmployees,
    createEmployee,
    updateEmployeeRole,
    removeEmployee,

    // Category actions
    fetchCategories,
    createCategory,
    updateCategory,
    deleteCategory,

    // Product actions
    fetchProducts,
    fetchProductDetail,
    fetchInternalProducts,
    createProduct,
    updateProduct,
    deleteProduct, 

    // Helpers
    setActiveStore,
    clearError,
  };
});