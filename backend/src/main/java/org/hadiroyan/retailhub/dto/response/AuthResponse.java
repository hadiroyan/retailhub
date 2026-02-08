package org.hadiroyan.retailhub.dto.response;

public class AuthResponse {
    public String token;
    public UserResponse user;

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    public AuthResponse() {
    }
}
