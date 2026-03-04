package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * A block of time indicating availability.
 */
public class AvailabilityBlock {

    @Schema(description = "Start time of availability block")
    private LocalDateTime start;

    @Schema(description = "End time of availability block")
    private LocalDateTime end;

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
}
