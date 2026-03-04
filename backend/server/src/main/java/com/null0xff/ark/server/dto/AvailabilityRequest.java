package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Request payload to submit user availability for a schedule.
 */
@Schema(description = "Request payload to submit user availability")
public class AvailabilityRequest {

    @Schema(description = "List of available time blocks")
    private List<AvailabilityBlock> blocks;

    public List<AvailabilityBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<AvailabilityBlock> blocks) {
        this.blocks = blocks;
    }
}
