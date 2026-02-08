package org.hadiroyan.retailhub.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.hadiroyan.retailhub.model.User;

public class UserResponse {

    public UUID id;
    public String email;
    public String fullName;
    public Boolean enabled;
    public Boolean emailVerified;
    public Set<String> roles;
    public LocalDateTime createdAt;

    public UserResponse() {
    }

    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.id = user.id;
        response.email = user.email;
        response.fullName = user.fullName;
        response.enabled = user.enabled;
        response.emailVerified = user.emailVerified;
        response.createdAt = user.createdAt;

        response.roles = user.userRoles.stream()
                .map(ur -> ur.role.name)
                .collect(java.util.stream.Collectors.toSet());

        return response;
    }
}