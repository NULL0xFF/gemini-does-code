package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

/**
 * Response payload representing a user's availability.
 */
@Schema(description = "User's availability for a schedule")
public class AvailabilityResponse {

    @Schema(description = "The User ID")
    private UUID userId;

    @Schema(description = "List of available time blocks")
    private List<AvailabilityBlock> blocks;

    public AvailabilityResponse(UUID userId, List<AvailabilityBlock> blocks) {
        this.userId = userId;
        this.blocks = blocks;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<AvailabilityBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<AvailabilityBlock> blocks) {
        this.blocks = blocks;
    }
}
