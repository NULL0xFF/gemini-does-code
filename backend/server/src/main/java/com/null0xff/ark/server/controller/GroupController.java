package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.GroupRequest;
import com.null0xff.ark.server.dto.GroupResponse;
import com.null0xff.ark.server.entity.Group;
import com.null0xff.ark.server.service.GroupService;
import com.null0xff.ark.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for core group CRUD operations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
@Tag(name = "Groups", description = "Endpoints for managing group collectives")
public class GroupController {

    private final UserService userService;
    private final GroupService groupService;

    @Operation(summary = "List My Groups", description = "Returns a list of all groups the current user is a member of.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved groups")
    @GetMapping
    public ResponseEntity<List<GroupResponse>> listMyGroups(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(userService.getUserGroups(userId));
    }

    @Operation(summary = "Create Group", description = "Creates a new group and makes the caller the initial MANAGER.")
    @ApiResponse(responseCode = "200", description = "Group created successfully")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@AuthenticationPrincipal Jwt jwt, @RequestBody GroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Group group = userService.createGroup(userId, request.getName(), request.getDescription());
        return ResponseEntity.ok(new GroupResponse(group.getId(), group.getName(), group.getDescription()));
    }

    @Operation(summary = "Get Group Details", description = "Retrieves details of a specific group.")
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupDetails(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(groupService.getGroupDetails(groupId, userId));
    }

    @Operation(summary = "Update Group Settings", description = "Updates group name and description (MANAGER only).")
    @PutMapping("/{groupId}")
    public ResponseEntity<Void> updateGroup(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId, @RequestBody GroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        groupService.updateGroup(groupId, userId, request.getName(), request.getDescription());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete Group", description = "Permanently deletes the group and all associated data (MANAGER only).")
    @ApiResponse(responseCode = "200", description = "Group deleted successfully")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        groupService.deleteGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }
}
