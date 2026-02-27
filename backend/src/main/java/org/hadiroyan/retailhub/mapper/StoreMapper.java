package org.hadiroyan.retailhub.mapper;

import org.hadiroyan.retailhub.dto.response.StoreResponse;
import org.hadiroyan.retailhub.model.Store;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StoreMapper {

    public StoreResponse toResponse(Store store) {
        StoreResponse response = new StoreResponse();
        response.id = store.id;
        response.name = store.name;
        response.slug = store.slug;
        response.description = store.description;
        response.address = store.address;
        response.phone = store.phone;
        response.email = store.email;
        response.status = store.status;
        response.createdAt = store.createdAt;
        response.updatedAt = store.updatedAt;

        if (store.owner != null) {
            StoreResponse.OwnerInfo ownerInfo = new StoreResponse.OwnerInfo();
            ownerInfo.id = store.owner.id;
            ownerInfo.fullName = store.owner.fullName;
            ownerInfo.email = store.owner.email;
            response.owner = ownerInfo;
        }

        return response;
    }
}
