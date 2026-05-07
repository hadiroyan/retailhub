import api from "./api";
import { API_ENDPOINTS } from "../utils/constants";

export const orderService = {
    
    // Customer orders
    async createOrder(data) {
        const response = await api.post(API_ENDPOINTS.ORDERS.BASE, data);
        return response.data;
    },

    async listOrders(params = {}) {
        const response = await api.get(API_ENDPOINTS.ORDERS.BASE, { params });
        return response.data;
    },

    async getOrder(id) {
        const response = await api.get(API_ENDPOINTS.ORDERS.BY_ID(id));
        return response.data;
    },

    async cancelOrder(id) {
        const response = await api.delete(API_ENDPOINTS.ORDERS.BY_ID(id));
        return response.data;
    },

    // Store orders
    async listStoreOrders(storeId, params = {}) {
        const response = await api.get(
            API_ENDPOINTS.ORDERS.STORE_ORDERS(storeId),
            { params }
        );
        return response.data;
    },

    async getStoreOrder(storeId, id) {
        const response = await api.get(
            API_ENDPOINTS.ORDERS.STORE_ORDER_BY_ID(storeId, id)
        );
        return response.data;
    },

    async updateOrderStatus(storeId, id, data) {
        const response = await api.patch(
            API_ENDPOINTS.ORDERS.STORE_ORDER_STATUS(storeId, id),
            data
        );
        return response.data;
    },
};

export default orderService;