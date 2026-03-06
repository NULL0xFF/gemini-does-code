package com.null0xff.ark.server.dto;

import com.null0xff.ark.server.enums.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Request to update a member's role within a group")
public class RoleUpdateRequest {

    @Schema(description = "The new role to assign", example = "AUDITOR")
    private GroupRole role;
}
