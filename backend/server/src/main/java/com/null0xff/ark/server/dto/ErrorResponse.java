package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standardized error response payload for the API.
 */
@Schema(description = "Standardized error response payload")
public class ErrorResponse {
    
    @Schema(description = "The error message", example = "User not found")
    private String message;

    @Schema(description = "The HTTP status code", example = "400")
    private int status;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
