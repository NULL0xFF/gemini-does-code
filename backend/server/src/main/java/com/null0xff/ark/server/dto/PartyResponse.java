package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response payload representing a party.
 */
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

    public PartyResponse(UUID id, UUID scheduleId, String title, String raidType, Integer members, Integer max, LocalDateTime start, List<String> joinedMembers) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.title = title;
        this.raidType = raidType;
        this.members = members;
        this.max = max;
        this.start = start != null ? start.toString() : null;
        this.joinedMembers = joinedMembers;
        
        if (start != null) {
            if (LocalDateTime.now().isAfter(start)) {
                this.status = "On-going";
            } else {
                this.status = "Planned";
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

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

    public Integer getMembers() {
        return members;
    }

    public void setMembers(Integer members) {
        this.members = members;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public List<String> getJoinedMembers() {
        return joinedMembers;
    }

    public void setJoinedMembers(List<String> joinedMembers) {
        this.joinedMembers = joinedMembers;
    }
}
