package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Schema(description = "Response payload representing a party")
public class PartyResponse {

    private UUID id;
    private UUID scheduleId;
    private String title;
    private String raidType;
    private Integer members;
    private Integer max;
    private String status;
    private String start;
    private List<String> joinedMembers;

    public PartyResponse(UUID id, UUID scheduleId, String title, String raidType, Integer members, Integer max, Instant start, List<String> joinedMembers) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.title = title;
        this.raidType = raidType;
        this.members = members;
        this.max = max;
        this.start = start != null ? start.toString() : null;
        this.joinedMembers = joinedMembers;

        if (start != null) {
            if (Instant.now().isAfter(start)) {
                this.status = "On-going";
            } else {
                this.status = "Planned";
            }
        }
    }
}
