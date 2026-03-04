package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.PartyRequest;
import com.null0xff.ark.server.dto.PartyResponse;
import com.null0xff.ark.server.service.PartyService;
import com.null0xff.ark.server.dto.AvailabilityRequest;
import com.null0xff.ark.server.dto.AvailabilityResponse;
import com.null0xff.ark.server.service.AvailabilityService;
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
 * Controller for managing schedule availability and schedule-related parties.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
@Tag(name = "Schedules & Availability", description = "Endpoints for managing time blocks, heatmaps, and schedule parties")
public class ScheduleController {

    private final AvailabilityService availabilityService;
    private final PartyService partyService;

    @Operation(summary = "Update My Availability", description = "Overwrites the current user's availability for a schedule.")
    @ApiResponse(responseCode = "200", description = "Availability updated successfully")
    @PutMapping("/{scheduleId}/availability/me")
    public ResponseEntity<Void> updateMyAvailability(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID scheduleId, @RequestBody AvailabilityRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        availabilityService.updateAvailability(scheduleId, userId, request.getBlocks());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get Aggregated Availability", description = "Retrieves all availability blocks for all members in a schedule.")
    @GetMapping("/{scheduleId}/availability")
    public ResponseEntity<List<AvailabilityResponse>> getAggregatedAvailability(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(availabilityService.getAggregatedAvailability(scheduleId, userId));
    }

    @Operation(summary = "Create Party", description = "Creates a new party for the schedule (MANAGER only).")
    @PostMapping("/{scheduleId}/parties")
    public ResponseEntity<PartyResponse> createParty(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID scheduleId, @RequestBody PartyRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(partyService.createParty(scheduleId, userId, request));
    }

    @Operation(summary = "List Parties", description = "Retrieves all parties for a specific schedule.")
    @GetMapping("/{scheduleId}/parties")
    public ResponseEntity<List<PartyResponse>> getScheduleParties(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(partyService.getScheduleParties(scheduleId, userId));
    }
}
