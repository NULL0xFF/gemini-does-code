package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.PartyCompleteRequest;
import com.null0xff.ark.server.service.PartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller for individual party interactions.
 *
 * <p>Join and leave are open to any group member. Delete and completion-status updates
 * require the {@code MANAGER} or {@code AUDITOR} role.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parties")
@Tag(name = "Parties", description = "Endpoints for joining, leaving, and managing raid parties")
public class PartyController {

  private final PartyService partyService;

  /**
   * Joins a party if it is not full. Open to any group member.
   *
   * @param jwt     the caller's JWT
   * @param partyId the party's unique identifier
   * @return 200 OK on success
   */
  @Operation(summary = "Join Party", description = "Joins a party if it is not already full.")
  @ApiResponse(responseCode = "200", description = "Joined the party successfully")
  @ApiResponse(responseCode = "400", description = "Party is full or user is already a member")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller is not a member of the owning group")
  @ApiResponse(responseCode = "404", description = "Party not found")
  @PostMapping("/{partyId}/join")
  public ResponseEntity<Void> joinParty(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID partyId) {
    UUID userId = UUID.fromString(jwt.getSubject());
    partyService.joinParty(partyId, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * Leaves a party the caller has previously joined.
   *
   * @param jwt     the caller's JWT
   * @param partyId the party's unique identifier
   * @return 200 OK on success
   */
  @Operation(summary = "Leave Party", description = "Removes the caller from a party they have joined.")
  @ApiResponse(responseCode = "200", description = "Left the party successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller is not a member of the party")
  @ApiResponse(responseCode = "404", description = "Party not found")
  @PostMapping("/{partyId}/leave")
  public ResponseEntity<Void> leaveParty(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID partyId) {
    UUID userId = UUID.fromString(jwt.getSubject());
    partyService.leaveParty(partyId, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * Permanently deletes a party. Requires {@code MANAGER} or {@code AUDITOR} role.
   *
   * @param jwt     the caller's JWT
   * @param partyId the party's unique identifier
   * @return 200 OK on success
   */
  @Operation(summary = "Delete Party", description = "Permanently deletes a raid party. Requires MANAGER or AUDITOR role.")
  @ApiResponse(responseCode = "200", description = "Party deleted successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller lacks MANAGER or AUDITOR role")
  @ApiResponse(responseCode = "404", description = "Party not found")
  @DeleteMapping("/{partyId}")
  public ResponseEntity<Void> deleteParty(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID partyId) {
    UUID userId = UUID.fromString(jwt.getSubject());
    partyService.deleteParty(partyId, userId);
    return ResponseEntity.ok().build();
  }

  /**
   * Marks a party as done or reverts it to planned. Requires {@code MANAGER} or {@code AUDITOR} role.
   *
   * @param jwt     the caller's JWT
   * @param partyId the party's unique identifier
   * @param request whether the party should be marked completed
   * @return 200 OK on success
   */
  @Operation(summary = "Update Party Completion", description = "Marks a party as done or reverts it to planned. Requires MANAGER or AUDITOR role.")
  @ApiResponse(responseCode = "200", description = "Completion status updated successfully")
  @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
  @ApiResponse(responseCode = "403", description = "Caller lacks MANAGER or AUDITOR role")
  @ApiResponse(responseCode = "404", description = "Party not found")
  @PatchMapping("/{partyId}/complete")
  public ResponseEntity<Void> completeParty(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID partyId,
      @RequestBody PartyCompleteRequest request) {
    UUID userId = UUID.fromString(jwt.getSubject());
    partyService.markPartyAsDone(partyId, userId, request.getCompleted());
    return ResponseEntity.ok().build();
  }
}
