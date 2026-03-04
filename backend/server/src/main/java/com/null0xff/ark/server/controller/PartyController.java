package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.service.PartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for managing individual party interactions.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parties")
@Tag(name = "Parties", description = "Endpoints for joining and leaving specific raid parties")
public class PartyController {

    private final PartyService partyService;

    @Operation(summary = "Join Party", description = "Joins a specific party if it is not full.")
    @ApiResponse(responseCode = "200", description = "Successfully joined party")
    @PostMapping("/{partyId}/join")
    public ResponseEntity<Void> joinParty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID partyId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        partyService.joinParty(partyId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Leave Party", description = "Leaves a specific party.")
    @ApiResponse(responseCode = "200", description = "Successfully left party")
    @PostMapping("/{partyId}/leave")
    public ResponseEntity<Void> leaveParty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID partyId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        partyService.leaveParty(partyId, userId);
        return ResponseEntity.ok().build();
    }
}
