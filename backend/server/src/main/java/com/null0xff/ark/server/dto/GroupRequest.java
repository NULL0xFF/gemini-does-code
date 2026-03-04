package com.null0xff.ark.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for creating a new group.
 */
@Schema(description = "Request payload for group creation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRequest {

    @Schema(description = "The name of the group", example = "Friday Night Static", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Optional description of the group", example = "Weekly raids and horizontal content.")
    private String description;




}
