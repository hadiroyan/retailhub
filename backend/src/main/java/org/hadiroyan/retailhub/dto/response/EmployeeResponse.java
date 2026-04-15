package org.hadiroyan.retailhub.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class EmployeeResponse {

    public UUID id;
    public String fullName;
    public String email;
    public String role;
    public boolean emailVerified;
    public LocalDateTime createdAt;

}
