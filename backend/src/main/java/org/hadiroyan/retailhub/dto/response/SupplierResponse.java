package org.hadiroyan.retailhub.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class SupplierResponse {

    public UUID id;
    public String name;
    public String contactPerson;
    public String email;
    public String phone;
    public String address;
    public String notes;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}