package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Request payload for creating a new schedule.
 */
@Schema(description = "Request payload for creating a new schedule")
public class ScheduleRequest {

    @Schema(description = "The title of the schedule", example = "March Week 1 Reset", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "The start date and time of the schedule in ISO format", example = "2026-03-04T10:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @Schema(description = "The end date and time of the schedule in ISO format", example = "2026-03-11T05:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
