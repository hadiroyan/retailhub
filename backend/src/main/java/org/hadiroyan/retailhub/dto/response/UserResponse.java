package org.hadiroyan.retailhub.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.model.User;

public class UserResponse {

    public UUID id;
    public String email;
    public String fullName;
    public String phone;
    public String address;
    public Boolean enabled;
    public Boolean emailVerified;
    public Set<String> roles;
    public LocalDateTime createdAt;

    public StoreInfo assignedStore; // untuk ADMIN, MANAGER, STAFF
    public List<StoreInfo> stores; // untuk OWNER

    public UserResponse() {
    }

    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.id = user.id;
        response.email = user.email;
        response.fullName = user.fullName;
        response.phone = user.phone;
        response.address = user.address;
        response.enabled = user.enabled;
        response.emailVerified = user.emailVerified;
        response.createdAt = user.createdAt;

        response.roles = user.userRoles.stream()
                .map(ur -> ur.role.name)
                .collect(java.util.stream.Collectors.toSet());

        return response;
    }

    public static class StoreInfo {
        public String id;
        public String name;
        public String slug;

        public StoreInfo(String id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }
    }
}