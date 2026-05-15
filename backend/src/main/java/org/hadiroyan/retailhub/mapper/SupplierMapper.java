package org.hadiroyan.retailhub.mapper;

import org.hadiroyan.retailhub.dto.response.SupplierResponse;
import org.hadiroyan.retailhub.model.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.id = supplier.id;
        response.name = supplier.name;
        response.contactPerson = supplier.contactPerson;
        response.email = supplier.email;
        response.phone = supplier.phone;
        response.address = supplier.address;
        response.notes = supplier.notes;
        response.createdAt = supplier.createdAt;
        response.updatedAt = supplier.updatedAt;

        return response;
    }
}
