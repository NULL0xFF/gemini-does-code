package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response payload representing an active invite code.
 */
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

    public InviteCodeResponse(String code, Integer max, Integer used, LocalDateTime expiresAt) {
        this.code = code;
        this.max = max;
        this.used = used;
        this.expires = expiresAt != null ? expiresAt.toLocalDate().toString() : null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }
}
