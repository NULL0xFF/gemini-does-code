package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Response payload for group information.
 */
@Schema(description = "Response payload for group details")
public class GroupResponse {

    @Schema(description = "The unique identifier of the group")
    private UUID id;

    @Schema(description = "The name of the group")
    private String name;

    @Schema(description = "The description of the group")
    private String description;

    public GroupResponse(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
