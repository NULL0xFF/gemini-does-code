package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.GroupRequest;
import com.null0xff.ark.server.dto.GroupResponse;
import com.null0xff.ark.server.entity.Group;
import com.null0xff.ark.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing groups and memberships.
 */
@RestController
@RequestMapping("/api/groups")
@Tag(name = "Groups", description = "Endpoints for managing group collectives and memberships")
public class GroupController {

    private final UserService userService;

    public GroupController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all groups that the currently authenticated user belongs to.
     *
     * @param jwt The authenticated user's JWT
     * @return A list of groups the user is a member of
     */
    @Operation(summary = "List My Groups", description = "Returns a list of all groups the current user is a member of.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved groups")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping
    public ResponseEntity<List<GroupResponse>> listMyGroups(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.getUserGroups(userId));
    }

    /**
     * Creates a new group and assigns the authenticated user as the MANAGER.
     *
     * @param jwt     The authenticated user's JWT
     * @param request The group creation details
     * @return The details of the newly created group
     */
    @Operation(summary = "Create Group", description = "Creates a new group and makes the caller the initial MANAGER.")
    @ApiResponse(responseCode = "200", description = "Group created successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@AuthenticationPrincipal Jwt jwt, @RequestBody GroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Group group = userService.createGroup(userId, request.getName(), request.getDescription());
        
        return ResponseEntity.ok(new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription()
        ));
    }
}
