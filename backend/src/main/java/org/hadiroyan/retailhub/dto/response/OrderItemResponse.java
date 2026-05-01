package org.hadiroyan.retailhub.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemResponse {

    public UUID id;
    public UUID productId;
    public String sku;
    public String name;
    public String imageUrl; // first image from product
    public Integer quantity;
    public BigDecimal unitPrice;
    public BigDecimal subtotal;
}