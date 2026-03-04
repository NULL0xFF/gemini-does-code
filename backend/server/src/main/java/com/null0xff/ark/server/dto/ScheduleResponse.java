package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
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

    public ScheduleResponse(UUID id, String title, Instant start, Instant end) {
        this.id = id;
        this.title = title;
        this.start = start != null ? start.toString() : null;
        this.end = end != null ? end.toString() : null;

        Instant now = Instant.now();
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
}
