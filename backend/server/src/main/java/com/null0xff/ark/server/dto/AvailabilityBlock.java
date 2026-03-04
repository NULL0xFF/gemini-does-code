package com.null0xff.ark.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * A block of time indicating availability.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityBlock {

    @Schema(description = "Start time of availability block")
    private LocalDateTime start;

    @Schema(description = "End time of availability block")
    private LocalDateTime end;




}
