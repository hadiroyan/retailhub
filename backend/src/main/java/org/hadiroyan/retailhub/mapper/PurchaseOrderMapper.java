package org.hadiroyan.retailhub.mapper;

import org.hadiroyan.retailhub.dto.response.PurchaseOrderItemResponse;
import org.hadiroyan.retailhub.dto.response.PurchaseOrderResponse;
import org.hadiroyan.retailhub.model.PurchaseOrder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PurchaseOrderMapper {

    public PurchaseOrderResponse toResponse(PurchaseOrder order) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.id = order.id;
        response.orderNumber = order.orderNumber;
        response.status = order.status;
        response.totalAmount = order.totalAmount;
        response.expectedDeliveryDate = order.expectedDeliveryDate;
        response.notes = order.notes;
        response.createdAt = order.createdAt;
        response.updatedAt = order.updatedAt;

        // Supplier info
        PurchaseOrderResponse.SupplierInfo supplierInfo = new PurchaseOrderResponse.SupplierInfo();
        supplierInfo.id = order.supplier.id;
        supplierInfo.name = order.supplier.name;
        supplierInfo.contactPerson = order.supplier.contactPerson;
        supplierInfo.phone = order.supplier.phone;
        response.supplier = supplierInfo;

        // Items
        response.items = order.items.stream().map(item -> {
            PurchaseOrderItemResponse itemResponse = new PurchaseOrderItemResponse();
            itemResponse.id = item.id;
            itemResponse.productId = item.product.id;
            itemResponse.sku = item.product.sku;
            itemResponse.name = item.product.name;
            itemResponse.quantity = item.quantity;
            itemResponse.unitPrice = item.unitPrice;
            itemResponse.subtotal = item.subtotal;
            return itemResponse;
        }).toList();

        return response;
    }
}