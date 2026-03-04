package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response payload representing a schedule instance.
 */
@Schema(description = "Response payload representing a schedule instance")
public class ScheduleResponse {

    @Schema(description = "The unique identifier of the schedule")
    private UUID id;

    @Schema(description = "The title of the schedule")
    private String title;

    @Schema(description = "The start date and time of the schedule")
    private String start;

    @Schema(description = "The end date and time of the schedule")
    private String end;

    @Schema(description = "The status of the schedule (e.g., ACTIVE, PLANNED, PAST)")
    private String status;

    public ScheduleResponse(UUID id, String title, LocalDateTime start, LocalDateTime end) {
        this.id = id;
        this.title = title;
        this.start = start != null ? start.toString() : null;
        this.end = end != null ? end.toString() : null;
        
        LocalDateTime now = LocalDateTime.now();
        if (start != null && end != null) {
            if (now.isBefore(start)) {
                this.status = "PLANNED";
            } else if (now.isAfter(end)) {
                this.status = "PAST";
            } else {
                this.status = "ACTIVE";
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
