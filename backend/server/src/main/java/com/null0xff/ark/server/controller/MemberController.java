package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.GroupMemberResponse;
import com.null0xff.ark.server.dto.RoleUpdateRequest;
import com.null0xff.ark.server.service.MemberService;
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
 * Controller for group roster management.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/members")
@Tag(name = "Members", description = "Endpoints for managing group membership and roles")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "List Group Members", description = "Returns the roster for the specified group.")
    @ApiResponse(responseCode = "200", description = "Members retrieved successfully")
    @GetMapping
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(memberService.getGroupMembers(groupId, userId));
    }

    @Operation(summary = "Remove Member / Leave Group", description = "Removes a member from the group. Users can remove themselves; MANAGERs can remove anyone.")
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID targetUserId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        memberService.removeMember(groupId, userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update Member Role", description = "Changes a member's role to AUDITOR or MEMBER (MANAGER only).")
    @ApiResponse(responseCode = "200", description = "Role updated successfully")
    @PatchMapping("/{targetUserId}/role")
    public ResponseEntity<Void> updateMemberRole(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID targetUserId,
            @RequestBody RoleUpdateRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        memberService.updateMemberRole(groupId, userId, targetUserId, request.getRole());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Transfer Manager Role", description = "Transfers MANAGER role to another member and demotes the caller to MEMBER (MANAGER only).")
    @ApiResponse(responseCode = "200", description = "Role transferred successfully")
    @PostMapping("/{targetUserId}/transfer")
    public ResponseEntity<Void> transferManager(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID targetUserId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        memberService.transferManager(groupId, userId, targetUserId);
        return ResponseEntity.ok().build();
    }
}
