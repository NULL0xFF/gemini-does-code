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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controller for group invite code management and the join-by-code flow.
 *
 * <p>Code generation, listing, and revocation require the {@code MANAGER} or
 * {@code AUDITOR} role. Joining via code is open to any authenticated user.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Invites", description = "Endpoints for managing group invite codes")
public class InviteController {

  private final InviteService inviteService;

  /**
   * Generates a new invite code for the group. Requires {@code MANAGER} or {@code AUDITOR} role.
   *
   * @param jwt     the caller's JWT
   * @param groupId the group's unique identifier
   * @param request max usage count and expiration days
   * @return the generated invite code details
   */
  @Operation(summary = "Generate Invite Code", description = "Generates a new invite code for the group. Requires MANAGER or AUDITOR role.")
  @ApiResponse(responseCode = "200", description = "Invite code generated successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller lacks MANAGER or AUDITOR role")
  @PostMapping("/api/groups/{groupId}/invites")
  public ResponseEntity<InviteCodeResponse> generateInviteCode(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID groupId,
      @RequestBody InviteCodeRequest request) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return ResponseEntity.ok(
        inviteService.generateInviteCode(groupId, userId, request.getMaxUsage(), request.getExpirationDays()));
  }

  /**
   * Returns all active invite codes for the group. Requires {@code MANAGER} or {@code AUDITOR} role.
   *
   * @param jwt     the caller's JWT
   * @param groupId the group's unique identifier
   * @return a list of active (non-expired, non-exhausted) invite codes
   */
  @Operation(summary = "List Active Invites", description = "Returns all active invite codes for the group. Requires MANAGER or AUDITOR role.")
  @ApiResponse(responseCode = "200", description = "Invite codes retrieved successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller lacks MANAGER or AUDITOR role")
  @GetMapping("/api/groups/{groupId}/invites")
  public ResponseEntity<List<InviteCodeResponse>> getActiveInviteCodes(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID groupId) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return ResponseEntity.ok(inviteService.getActiveInviteCodes(groupId, userId));
  }

  /**
   * Revokes a specific invite code. Requires {@code MANAGER} or {@code AUDITOR} role.
   *
   * @param jwt     the caller's JWT
   * @param groupId the group's unique identifier
   * @param code    the invite code string to revoke
   * @return 200 OK on success
   */
  @Operation(summary = "Revoke Invite Code", description = "Permanently revokes an invite code. Requires MANAGER or AUDITOR role.")
  @ApiResponse(responseCode = "200", description = "Invite code revoked successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller lacks MANAGER or AUDITOR role, or code belongs to a different group")
  @ApiResponse(responseCode = "404", description = "Invite code not found")
  @DeleteMapping("/api/groups/{groupId}/invites/{code}")
  public ResponseEntity<Void> revokeInviteCode(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID groupId,
      @PathVariable String code) {
    UUID userId = UUID.fromString(jwt.getSubject());
    inviteService.revokeInviteCode(groupId, userId, code);
    return ResponseEntity.ok().build();
  }

  /**
   * Joins a group using a valid invite code. Open to any authenticated user.
   *
   * @param jwt     the caller's JWT
   * @param request the invite code string
   * @return 200 OK on success
   */
  @Operation(summary = "Join Group", description = "Joins a group using a valid invite code.")
  @ApiResponse(responseCode = "200", description = "Joined the group successfully")
  @ApiResponse(responseCode = "400", description = "Code is invalid, expired, exhausted, or user is already a member")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @PostMapping("/api/groups/join")
  public ResponseEntity<Void> joinGroup(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody JoinGroupRequest request) {
    UUID userId = UUID.fromString(jwt.getSubject());
    inviteService.joinGroup(userId, request.getCode());
    return ResponseEntity.ok().build();
  }
}
