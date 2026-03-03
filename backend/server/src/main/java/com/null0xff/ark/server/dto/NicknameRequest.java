package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request payload for updating a user's nickname.
 */
@Schema(description = "Request payload for updating the user's custom nickname")
public class NicknameRequest {
    
    @Schema(description = "The new nickname to set. Can be empty to revert to Discord username.", example = "CoolAdventurer")
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
