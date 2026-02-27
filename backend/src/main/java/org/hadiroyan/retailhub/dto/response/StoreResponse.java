package org.hadiroyan.retailhub.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreResponse {

    public UUID id;
    public String name;
    public String slug;
    public String description;
    public String address;
    public String phone;
    public String email;
    public String status;
    public OwnerInfo owner;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OwnerInfo {
        public UUID id;
        public String fullName;
        public String email;
    }
}
