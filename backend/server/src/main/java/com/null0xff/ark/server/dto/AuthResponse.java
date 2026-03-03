package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for authentication responses.
 * Contains the JWT token used for authenticating subsequent API requests.
 */
@Schema(description = "Response payload containing the generated JWT token")
public class AuthResponse {
    
    @Schema(description = "The JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
