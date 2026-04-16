package org.hadiroyan.retailhub.mapper;

import org.hadiroyan.retailhub.dto.response.EmployeeResponse;
import org.hadiroyan.retailhub.model.User;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmployeeMapper {

    public EmployeeResponse toResponse(User user) {
        EmployeeResponse response = new EmployeeResponse();
        response.id = user.id;
        response.fullName = user.fullName;
        response.email = user.email;
        response.emailVerified = user.emailVerified;
        response.createdAt = user.createdAt;

        response.role = user.userRoles.stream().map(ur -> ur.role.name).findFirst().orElse(null);
        return response;
    }
}
