package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for generating a new invite code.
 */
@Schema(description = "Request payload for generating a new invite code")
public class InviteCodeRequest {

    @Schema(description = "Maximum number of times this code can be used", example = "5")
    private Integer maxUsage;

    @Schema(description = "Number of days until the code expires", example = "7")
    private Integer expirationDays;

    public Integer getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(Integer maxUsage) {
        this.maxUsage = maxUsage;
    }

    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }
}
