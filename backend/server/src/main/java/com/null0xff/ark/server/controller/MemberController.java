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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for group roster management.
 *
 * <p>Roster read access requires membership. Kick and role-change operations require
 * the {@code MANAGER} role. Members may remove themselves without elevated privileges.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/members")
@Tag(name = "Members", description = "Endpoints for managing group membership and roles")
public class MemberController {

  private final MemberService memberService;

  /**
   * Returns the full roster for the specified group. Requires membership.
   *
   * @param jwt     the caller's JWT
   * @param groupId the group's unique identifier
   * @return a list of member summaries
   */
  @Operation(summary = "List Group Members", description = "Returns the full roster for the specified group. Requires membership.")
  @ApiResponse(responseCode = "200", description = "Members retrieved successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller is not a member of the group")
  @GetMapping
  public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID groupId) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return ResponseEntity.ok(memberService.getGroupMembers(groupId, userId));
  }

  /**
   * Removes a member from the group. Members may remove themselves; only a {@code MANAGER}
   * may remove other members.
   *
   * @param jwt          the caller's JWT
   * @param groupId      the group's unique identifier
   * @param targetUserId the user to remove
   * @return 200 OK on success
   */
  @Operation(summary = "Remove Member / Leave Group", description = "Removes a member from the group. Members may remove themselves; MANAGER can remove anyone.")
  @ApiResponse(responseCode = "200", description = "Member removed successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller lacks permission to remove this member")
  @ApiResponse(responseCode = "404", description = "Member not found in the group")
  @DeleteMapping("/{targetUserId}")
  public ResponseEntity<Void> removeMember(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID groupId,
      @PathVariable UUID targetUserId) {
    UUID userId = UUID.fromString(jwt.getSubject());
    memberService.removeMember(groupId, userId, targetUserId);
    return ResponseEntity.ok().build();
  }

  /**
   * Changes a member's role to {@code AUDITOR} or {@code MEMBER}. Requires {@code MANAGER} role.
   * Promoting to {@code MANAGER} is handled by the transfer endpoint.
   *
   * @param jwt          the caller's JWT
   * @param groupId      the group's unique identifier
   * @param targetUserId the user whose role is to be updated
   * @param request      the new role
   * @return 200 OK on success
   */
  @Operation(summary = "Update Member Role", description = "Changes a member's role to AUDITOR or MEMBER. Requires MANAGER role.")
  @ApiResponse(responseCode = "200", description = "Role updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid role or attempting to self-update")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller does not hold the MANAGER role")
  @ApiResponse(responseCode = "404", description = "Member not found in the group")
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

  /**
   * Transfers the {@code MANAGER} role to another member and demotes the caller to
   * {@code MEMBER}. Requires {@code MANAGER} role.
   *
   * @param jwt          the caller's JWT
   * @param groupId      the group's unique identifier
   * @param targetUserId the user to promote to {@code MANAGER}
   * @return 200 OK on success
   */
  @Operation(summary = "Transfer Manager Role", description = "Transfers MANAGER role to another member and demotes the caller to MEMBER. Requires MANAGER role.")
  @ApiResponse(responseCode = "200", description = "Management transferred successfully")
  @ApiResponse(responseCode = "400", description = "Target is the same as the caller")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller does not hold the MANAGER role")
  @ApiResponse(responseCode = "404", description = "Target user is not a member of the group")
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
