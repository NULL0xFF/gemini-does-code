package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Request payload to create a new party.
 */
public class PartyRequest {

    @Schema(description = "Title of the party")
    private String title;

    @Schema(description = "Type of raid")
    private String raidType;

    @Schema(description = "Maximum number of members")
    private Integer maxMembers;

    @Schema(description = "Exact start time of the party")
    private LocalDateTime startTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRaidType() {
        return raidType;
    }

    public void setRaidType(String raidType) {
        this.raidType = raidType;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
