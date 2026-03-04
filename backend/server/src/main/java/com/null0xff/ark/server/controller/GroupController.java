package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.*;
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
 * Controller for managing groups and memberships.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
@Tag(name = "Groups", description = "Endpoints for managing group collectives and memberships")
public class GroupController {

    private final UserService userService;
    private final GroupService groupService;

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

    @Operation(summary = "Get Group Members", description = "Retrieves the roster for a specific group.")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(groupService.getGroupMembers(groupId, userId));
    }

    @Operation(summary = "Generate Invite Code", description = "Generates a new invite code for the group (MANAGER only).")
    @PostMapping("/{groupId}/invites")
    public ResponseEntity<InviteCodeResponse> generateInviteCode(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId, @RequestBody InviteCodeRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(groupService.generateInviteCode(groupId, userId, request.getMaxUsage(), request.getExpirationDays()));
    }

    @Operation(summary = "List Active Invites", description = "Retrieves all active invite codes for the group (MANAGER only).")
    @GetMapping("/{groupId}/invites")
    public ResponseEntity<List<InviteCodeResponse>> getActiveInviteCodes(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(groupService.getActiveInviteCodes(groupId, userId));
    }

    @Operation(summary = "Revoke Invite Code", description = "Revokes a specific invite code (MANAGER only).")
    @DeleteMapping("/{groupId}/invites/{code}")
    public ResponseEntity<Void> revokeInviteCode(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId, @PathVariable String code) {
        UUID userId = UUID.fromString(jwt.getSubject());
        groupService.revokeInviteCode(groupId, userId, code);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Join Group", description = "Joins a group using an active invite code.")
    @PostMapping("/join")
    public ResponseEntity<Void> joinGroup(@AuthenticationPrincipal Jwt jwt, @RequestBody JoinGroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        groupService.joinGroup(userId, request.getCode());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Create Schedule", description = "Creates a new schedule for the group (MANAGER only).")
    @PostMapping("/{groupId}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId, @RequestBody ScheduleRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(groupService.createSchedule(groupId, userId, request.getTitle(), request.getStartTime(), request.getEndTime()));
    }

    @Operation(summary = "List Schedules", description = "Retrieves all schedules for a specific group.")
    @GetMapping("/{groupId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getGroupSchedules(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(groupService.getGroupSchedules(groupId, userId));
    }

    @Operation(summary = "Update Schedule", description = "Updates an existing schedule (MANAGER only).")
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> updateSchedule(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID scheduleId, @RequestBody ScheduleRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        groupService.updateSchedule(scheduleId, userId, request.getTitle(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete Schedule", description = "Permanently deletes a schedule and all its availability data (MANAGER only).")
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        groupService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.ok().build();
    }
}
