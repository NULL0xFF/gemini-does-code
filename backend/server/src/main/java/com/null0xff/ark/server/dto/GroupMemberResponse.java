package com.null0xff.ark.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Response payload representing a member of a group.
 */
@Schema(description = "Response payload representing a group member")
public class GroupMemberResponse {

    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "User's display name (nickname or fallback to discord username)")
    private String username;

    @Schema(description = "User's role in the group (MANAGER, MEMBER)")
    private String role;
    
    @Schema(description = "Discord user ID for avatar fallback")
    private String discordId;
    
    @Schema(description = "Discord avatar hash")
    private String avatar;

    public GroupMemberResponse(UUID id, String username, String role, String discordId, String avatar) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.discordId = discordId;
        this.avatar = avatar;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
