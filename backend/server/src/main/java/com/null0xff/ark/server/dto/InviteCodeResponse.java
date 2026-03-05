package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@Schema(description = "Response payload representing an invite code")
public class InviteCodeResponse {

    @Schema(description = "The unique invite code string")
    private String code;

    @Schema(description = "Maximum number of times this code can be used")
    private Integer max;

    @Schema(description = "Number of times this code has already been used")
    private Integer used;

    @Schema(description = "The date and time the code expires (YYYY-MM-DD)", type = "string", format = "date")
    private String expires;

    public InviteCodeResponse(String code, Integer max, Integer used, Instant expiresAt) {
        this.code = code;
        this.max = max;
        this.used = used;
        this.expires = expiresAt != null ? expiresAt.atZone(java.time.ZoneOffset.UTC).toLocalDate().toString() : null;
    }
}
