package com.null0xff.ark.server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for handling user-specific operations.
 * Requires an authenticated user session (JWT).
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "Endpoints for retrieving and managing user profile information")
public class UserController {

    /**
     * Retrieves the profile information of the currently authenticated user.
     * Extracts data directly from the verified JWT claims.
     *
     * @param jwt The decoded JWT token representing the current user
     * @return A map containing the user's ID, Discord ID, username, and avatar hash
     */
    @Operation(summary = "Get Current User", description = "Returns the profile details of the user associated with the provided JWT access token.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "id", jwt.getSubject(),
                "discordId", jwt.getClaim("discordId"),
                "username", jwt.getClaim("username"),
                "avatar", jwt.getClaim("avatar")
        );
    }
}
