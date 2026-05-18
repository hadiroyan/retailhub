import api from "./api";
import { API_ENDPOINTS } from "../utils/constants";

export const supplierService = {
    // Suppliers
    async createSupplier(storeId, data) {
        const response = await api.post(API_ENDPOINTS.SUPPLIERS.BASE(storeId), data);
        return response.data;
    },
    async listSuppliers(storeId, params = {}) {
        const response = await api.get(API_ENDPOINTS.SUPPLIERS.BASE(storeId), { params });
        return response.data;
    },
    async getSupplier(storeId, id) {
        const response = await api.get(API_ENDPOINTS.SUPPLIERS.BY_ID(storeId, id));
        return response.data;
    },
    async updateSupplier(storeId, id, data) {
        const response = await api.put(API_ENDPOINTS.SUPPLIERS.BY_ID(storeId, id), data);
        return response.data;
    },
    async deleteSupplier(storeId, id) {
        const response = await api.delete(API_ENDPOINTS.SUPPLIERS.BY_ID(storeId, id));
        return response.data;
    },

    // Purchase Orders
    async createPurchaseOrder(storeId, data) {
        const response = await api.post(API_ENDPOINTS.PURCHASE_ORDERS.BASE(storeId), data);
        return response.data;
    },
    async listPurchaseOrders(storeId, params = {}) {
        const response = await api.get(API_ENDPOINTS.PURCHASE_ORDERS.BASE(storeId), { params });
        return response.data;
    },
    async getPurchaseOrder(storeId, id) {
        const response = await api.get(API_ENDPOINTS.PURCHASE_ORDERS.BY_ID(storeId, id));
        return response.data;
    },
    async updatePurchaseOrderStatus(storeId, id, data) {
        const response = await api.patch(API_ENDPOINTS.PURCHASE_ORDERS.STATUS(storeId, id), data);
        return response.data;
    },
};

export default supplierService;