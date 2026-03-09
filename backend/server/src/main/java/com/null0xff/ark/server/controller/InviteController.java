package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.InviteCodeRequest;
import com.null0xff.ark.server.dto.InviteCodeResponse;
import com.null0xff.ark.server.dto.JoinGroupRequest;
import com.null0xff.ark.server.service.InviteService;
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
 * Controller for group invite code management and join-by-code flow.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Invites", description = "Endpoints for managing group invite codes")
public class InviteController {

    private final InviteService inviteService;

    @Operation(summary = "Generate Invite Code", description = "Generates a new invite code for the group (MANAGER or AUDITOR only).")
    @ApiResponse(responseCode = "200", description = "Invite code generated successfully")
    @PostMapping("/api/groups/{groupId}/invites")
    public ResponseEntity<InviteCodeResponse> generateInviteCode(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId,
            @RequestBody InviteCodeRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(inviteService.generateInviteCode(groupId, userId, request.getMaxUsage(), request.getExpirationDays()));
    }

    @Operation(summary = "List Active Invites", description = "Retrieves all active invite codes for the group (MANAGER or AUDITOR only).")
    @ApiResponse(responseCode = "200", description = "Invite codes retrieved successfully")
    @GetMapping("/api/groups/{groupId}/invites")
    public ResponseEntity<List<InviteCodeResponse>> getActiveInviteCodes(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(inviteService.getActiveInviteCodes(groupId, userId));
    }

    @Operation(summary = "Revoke Invite Code", description = "Revokes a specific invite code (MANAGER or AUDITOR only).")
    @ApiResponse(responseCode = "200", description = "Invite code revoked successfully")
    @DeleteMapping("/api/groups/{groupId}/invites/{code}")
    public ResponseEntity<Void> revokeInviteCode(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId,
            @PathVariable String code) {
        UUID userId = UUID.fromString(jwt.getSubject());
        inviteService.revokeInviteCode(groupId, userId, code);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Join Group", description = "Joins a group using an active invite code.")
    @ApiResponse(responseCode = "200", description = "Joined group successfully")
    @PostMapping("/api/groups/join")
    public ResponseEntity<Void> joinGroup(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody JoinGroupRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        inviteService.joinGroup(userId, request.getCode());
        return ResponseEntity.ok().build();
    }
}
