package com.null0xff.ark.server.controller;

import com.null0xff.ark.server.dto.AvailabilityRequest;
import com.null0xff.ark.server.dto.AvailabilityResponse;
import com.null0xff.ark.server.dto.PartyRequest;
import com.null0xff.ark.server.dto.PartyResponse;
import com.null0xff.ark.server.dto.ScheduleRequest;
import com.null0xff.ark.server.dto.ScheduleResponse;
import com.null0xff.ark.server.service.AvailabilityService;
import com.null0xff.ark.server.service.PartyService;
import com.null0xff.ark.server.service.ScheduleService;
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
 * Controller for schedule CRUD, availability, and parties.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Schedules & Availability", description = "Endpoints for managing schedules, availability, and parties")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final AvailabilityService availabilityService;
    private final PartyService partyService;

    @Operation(summary = "Create Schedule", description = "Creates a new schedule for the group (MANAGER or AUDITOR only).")
    @ApiResponse(responseCode = "200", description = "Schedule created successfully")
    @PostMapping("/api/groups/{groupId}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId,
            @RequestBody ScheduleRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(scheduleService.createSchedule(groupId, userId, request.getTitle(), request.getStartTime(), request.getEndTime()));
    }

    @Operation(summary = "List Schedules", description = "Retrieves all schedules for a specific group.")
    @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully")
    @GetMapping("/api/groups/{groupId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getGroupSchedules(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID groupId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(scheduleService.getGroupSchedules(groupId, userId));
    }

    @Operation(summary = "Get Schedule Details", description = "Retrieves details of a specific schedule by its ID.")
    @GetMapping("/api/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getSchedule(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(scheduleService.getSchedule(scheduleId, userId));
    }

    @Operation(summary = "Update Schedule", description = "Updates an existing schedule (MANAGER or AUDITOR only).")
    @PutMapping("/api/schedules/{scheduleId}")
    public ResponseEntity<Void> updateSchedule(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID scheduleId,
            @RequestBody ScheduleRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        scheduleService.updateSchedule(scheduleId, userId, request.getTitle(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete Schedule", description = "Permanently deletes a schedule and all its data (MANAGER or AUDITOR only).")
    @ApiResponse(responseCode = "200", description = "Schedule deleted successfully")
    @DeleteMapping("/api/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update My Availability", description = "Overwrites the current user's availability for a schedule.")
    @ApiResponse(responseCode = "200", description = "Availability updated successfully")
    @PutMapping("/api/schedules/{scheduleId}/availability/me")
    public ResponseEntity<Void> updateMyAvailability(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID scheduleId,
            @RequestBody AvailabilityRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        availabilityService.updateAvailability(scheduleId, userId, request.getBlocks());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get Aggregated Availability", description = "Retrieves all availability blocks for all members in a schedule.")
    @GetMapping("/api/schedules/{scheduleId}/availability")
    public ResponseEntity<List<AvailabilityResponse>> getAggregatedAvailability(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(availabilityService.getAggregatedAvailability(scheduleId, userId));
    }

    @Operation(summary = "Get My Availability", description = "Retrieves the current user's previously submitted availability for a schedule.")
    @GetMapping("/api/schedules/{scheduleId}/availability/me")
    public ResponseEntity<AvailabilityResponse> getMyAvailability(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(availabilityService.getUserAvailability(scheduleId, userId));
    }

    @Operation(summary = "Create Party", description = "Creates a new party for the schedule (MANAGER or AUDITOR only).")
    @PostMapping("/api/schedules/{scheduleId}/parties")
    public ResponseEntity<PartyResponse> createParty(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID scheduleId,
            @RequestBody PartyRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(partyService.createParty(scheduleId, userId, request));
    }

    @Operation(summary = "List Parties", description = "Retrieves all parties for a specific schedule.")
    @GetMapping("/api/schedules/{scheduleId}/parties")
    public ResponseEntity<List<PartyResponse>> getScheduleParties(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID scheduleId) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(partyService.getScheduleParties(scheduleId, userId));
    }
}
