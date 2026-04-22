import api from "./api";
import { API_ENDPOINTS } from "../utils/constants";

export const storeService = {
  // =========================================================================
  // Store CRUD
  // =========================================================================

  async listStores(params = {}) {
    const response = await api.get(API_ENDPOINTS.STORES.BASE, { params });
    return response.data;
  },

  async getStoreBySlug(slug) {
    const response = await api.get(API_ENDPOINTS.STORES.BY_SLUG(slug));
    return response.data;
  },

  async createStore(data) {
    const response = await api.post(API_ENDPOINTS.STORES.BASE, data);
    return response.data;
  },

  async updateStore(id, data) {
    const response = await api.put(API_ENDPOINTS.STORES.BY_ID(id), data);
    return response.data;
  },

  async deleteStore(id) {
    const response = await api.delete(API_ENDPOINTS.STORES.BY_ID(id));
    return response.data;
  },

  async updateStoreStatus(id, status) {
    const response = await api.patch(API_ENDPOINTS.STORES.STATUS(id), {
      status,
    });
    return response.data;
  },

  // =========================================================================
  // Employee
  // =========================================================================

  async listEmployees(storeId, params = {}) {
    const response = await api.get(API_ENDPOINTS.EMPLOYEES.BASE(storeId), {
      params,
    });
    return response.data;
  },

  async createEmployee(storeId, data) {
    const response = await api.post(
      API_ENDPOINTS.EMPLOYEES.BASE(storeId),
      data
    );
    return response.data;
  },

  async updateEmployeeRole(storeId, userId, data) {
    const response = await api.put(
      API_ENDPOINTS.EMPLOYEES.BY_ID(storeId, userId),
      data
    );
    return response.data;
  },

  async removeEmployee(storeId, userId) {
    const response = await api.delete(
      API_ENDPOINTS.EMPLOYEES.BY_ID(storeId, userId)
    );
    return response.data;
  },

  // =========================================================================
  // Category
  // =========================================================================

  async listCategories(storeId, params = {}) {
    const response = await api.get(API_ENDPOINTS.CATEGORIES.BASE(storeId), {
      params,
    });
    return response.data;
  },

  async getCategoryBySlug(storeId, slug) {
    const response = await api.get(
      API_ENDPOINTS.CATEGORIES.BY_SLUG(storeId, slug)
    );
    return response.data;
  },

  async createCategory(storeId, data) {
    const response = await api.post(
      API_ENDPOINTS.CATEGORIES.BASE(storeId),
      data
    );
    return response.data;
  },

  async updateCategory(storeId, id, data) {
    const response = await api.put(
      API_ENDPOINTS.CATEGORIES.BY_ID(storeId, id),
      data
    );
    return response.data;
  },

  async deleteCategory(storeId, id) {
    const response = await api.delete(
      API_ENDPOINTS.CATEGORIES.BY_ID(storeId, id)
    );
    return response.data;
  },

  // =========================================================================
  // Product
  // =========================================================================

  async listProducts(storeId, params = {}) {
    const response = await api.get(API_ENDPOINTS.PRODUCTS.BASE(storeId), {
      params,
    });
    return response.data;
  },

  async getInternalProducts(storeId, params = {}) {
    const response = await api.get(
      API_ENDPOINTS.PRODUCTS.INTERNAL(storeId),
      { params }
    );
    return response.data;
  },

  async getProductBySku(storeId, sku) {
    const response = await api.get(
      API_ENDPOINTS.PRODUCTS.BY_SKU(storeId, sku)
    );
    return response.data;
  },

  async getProductDetail(storeId, sku) {
    const response = await api.get(
      API_ENDPOINTS.PRODUCTS.DETAIL(storeId, sku)
    );
    return response.data;
  },

  async createProduct(storeId, data) {
    const response = await api.post(
      API_ENDPOINTS.PRODUCTS.BASE(storeId),
      data
    );
    return response.data;
  },

  async updateProduct(storeId, id, data) {
    const response = await api.put(
      API_ENDPOINTS.PRODUCTS.BY_ID(storeId, id),
      data
    );
    return response.data;
  },

  async deleteProduct(storeId, id) {
    const response = await api.delete(
      API_ENDPOINTS.PRODUCTS.BY_ID(storeId, id)
    );
    return response.data;
  },
};

export default storeService;