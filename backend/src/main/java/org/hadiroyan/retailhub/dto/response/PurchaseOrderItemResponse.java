package org.hadiroyan.retailhub.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class PurchaseOrderItemResponse {

    public UUID id;
    public UUID productId;
    public String sku;
    public String name;
    public Integer quantity;
    public BigDecimal unitPrice;
    public BigDecimal subtotal;
}