package com.null0xff.ark.server.service;

import com.null0xff.ark.server.dto.*;
import com.null0xff.ark.server.entity.*;
import com.null0xff.ark.server.enums.GroupRole;
import com.null0xff.ark.server.exception.ForbiddenException;
import com.null0xff.ark.server.exception.ResourceNotFoundException;
import com.null0xff.ark.server.exception.ValidationException;
import com.null0xff.ark.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing groups, rosters, and invitations.
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final MemberAvailabilityRepository memberAvailabilityRepository;

    public GroupResponse getGroupDetails(UUID groupId, UUID userId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found", groupId));
        return new GroupResponse(group.getId(), group.getName(), group.getDescription());
    }

    @Transactional
    public void updateGroup(UUID groupId, UUID managerId, String name, String description) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can update group settings", groupId);
        }

        Group group = manager.getGroup();
        group.setName(name);
        group.setDescription(description);
        groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(UUID groupId, UUID managerId) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can delete the group", groupId);
        }

        groupRepository.deleteById(groupId);
    }

    public List<GroupMemberResponse> getGroupMembers(UUID groupId, UUID userId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(member -> new GroupMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getNickname() != null && !member.getUser().getNickname().isEmpty() ? member.getUser().getNickname() : member.getUser().getUsername(),
                        member.getRole().name(),
                        member.getUser().getDiscordId(),
                        member.getUser().getAvatar()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeMember(UUID groupId, UUID userId, UUID targetUserId) {
        GroupMember actor = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in group", targetUserId));

        // Permission check: Either user is leaving voluntarily, OR manager is kicking someone
        if (!userId.equals(targetUserId) && actor.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can remove other members", groupId);
        }

        // Prevent deleting the last manager if others exist (simplified: just don't allow manager to leave if they are the only manager)
        if (target.getRole() == GroupRole.MANAGER) {
            List<GroupMember> allMembers = groupMemberRepository.findByGroupId(groupId);
            long managerCount = allMembers.stream().filter(m -> m.getRole() == GroupRole.MANAGER).count();
            if (managerCount <= 1 && userId.equals(targetUserId)) {
                throw new ValidationException("Cannot leave group as the only manager. Delete the group instead or promote another member.", groupId);
            }
        }

        // Cleanup: Remove from all parties and availability in this group
        partyMemberRepository.deleteByUserIdAndGroupId(targetUserId, groupId);
        memberAvailabilityRepository.deleteByUserIdAndGroupId(targetUserId, groupId);
        
        // Remove membership
        groupMemberRepository.delete(target);
    }

    @Transactional
    public void transferManager(UUID groupId, UUID currentManagerId, UUID newManagerId) {
        if (currentManagerId.equals(newManagerId)) {
            throw new ValidationException("Cannot transfer management to yourself", groupId);
        }

        GroupMember currentManager = groupMemberRepository.findByGroupIdAndUserId(groupId, currentManagerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        if (currentManager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can transfer group ownership", groupId);
        }

        GroupMember newManager = groupMemberRepository.findByGroupIdAndUserId(groupId, newManagerId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this group", newManagerId));

        // Perform the transfer
        newManager.setRole(GroupRole.MANAGER);
        currentManager.setRole(GroupRole.MEMBER);

        groupMemberRepository.save(newManager);
        groupMemberRepository.save(currentManager);
    }

    @Transactional
    public void updateMemberRole(UUID groupId, UUID managerId, UUID targetUserId, GroupRole newRole) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can change member roles", groupId);
        }

        if (managerId.equals(targetUserId)) {
            throw new ValidationException("Cannot change your own role. Use the manager transfer feature instead.", groupId);
        }

        GroupMember target = groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in group", targetUserId));

        if (newRole == GroupRole.MANAGER) {
            throw new ValidationException("Use the transfer endpoint to promote someone to MANAGER", groupId);
        }

        target.setRole(newRole);
        groupMemberRepository.save(target);
    }

    @Transactional
    public InviteCodeResponse generateInviteCode(UUID groupId, UUID managerId, Integer maxUsage, Integer expirationDays) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can generate invite codes", groupId);
        }

        String codeStr = "ARK-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase() + "-" + UUID.randomUUID().toString().substring(4, 8).toUpperCase();
        
        InviteCode code = new InviteCode();
        code.setCode(codeStr);
        code.setGroup(manager.getGroup());
        code.setCreatedBy(manager.getUser());
        code.setMaxUsage(maxUsage);
        code.setExpiresAt(Instant.now().plus(expirationDays, ChronoUnit.DAYS));

        inviteCodeRepository.save(code);

        return new InviteCodeResponse(code.getCode(), code.getMaxUsage(), code.getCurrentUsage(), code.getExpiresAt());
    }

    public List<InviteCodeResponse> getActiveInviteCodes(UUID groupId, UUID managerId) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can view invite codes", groupId);
        }

        return inviteCodeRepository.findByGroupId(groupId).stream()
                .filter(code -> code.getExpiresAt().isAfter(Instant.now()) && (code.getMaxUsage() == null || code.getCurrentUsage() < code.getMaxUsage()))
                .map(code -> new InviteCodeResponse(code.getCode(), code.getMaxUsage(), code.getCurrentUsage(), code.getExpiresAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeInviteCode(UUID groupId, UUID managerId, String codeStr) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can revoke invite codes", groupId);
        }

        InviteCode code = inviteCodeRepository.findByCode(codeStr)
                .orElseThrow(() -> new ResourceNotFoundException("Code not found"));

        if (!code.getGroup().getId().equals(groupId)) {
            throw new ValidationException("Code does not belong to this group", groupId);
        }

        inviteCodeRepository.delete(code);
    }

    @Transactional
    public void joinGroup(UUID userId, String codeStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", userId));

        InviteCode code = inviteCodeRepository.findByCode(codeStr)
                .orElseThrow(() -> new ValidationException("Invalid or expired invite code"));

        if (code.getExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException("Invite code has expired");
        }

        if (code.getMaxUsage() != null && code.getCurrentUsage() >= code.getMaxUsage()) {
            throw new ValidationException("Invite code usage limit reached");
        }

        Optional<GroupMember> existingMember = groupMemberRepository.findByGroupIdAndUserId(code.getGroup().getId(), userId);
        if (existingMember.isPresent()) {
            throw new ValidationException("You are already a member of this group", code.getGroup().getId());
        }

        GroupMember newMember = new GroupMember();
        newMember.setGroup(code.getGroup());
        newMember.setUser(user);
        newMember.setRole(GroupRole.MEMBER);
        groupMemberRepository.save(newMember);

        code.setCurrentUsage(code.getCurrentUsage() + 1);
        inviteCodeRepository.save(code);
    }

    @Transactional
    public ScheduleResponse createSchedule(UUID groupId, UUID managerId, String title, Instant start, Instant end) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can create schedules", groupId);
        }

        if (start == null || end == null || !end.isAfter(start)) {
            throw new ValidationException("Invalid schedule time range", groupId);
        }

        ScheduleInstance schedule = new ScheduleInstance();
        schedule.setGroup(manager.getGroup());
        schedule.setTitle(title);
        schedule.setStartTime(start);
        schedule.setEndTime(end);

        scheduleRepository.save(schedule);

        return new ScheduleResponse(schedule.getId(), schedule.getTitle(), schedule.getStartTime(), schedule.getEndTime());
    }

    @Transactional
    public void updateSchedule(UUID scheduleId, UUID managerId, String title, Instant start, Instant end) {
        ScheduleInstance schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found", scheduleId));

        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(schedule.getGroup().getId(), managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", scheduleId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can update schedules", scheduleId);
        }

        if (start == null || end == null || !end.isAfter(start)) {
            throw new ValidationException("Invalid schedule time range", scheduleId);
        }

        schedule.setTitle(title);
        schedule.setStartTime(start);
        schedule.setEndTime(end);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(UUID scheduleId, UUID managerId) {
        ScheduleInstance schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found", scheduleId));

        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(schedule.getGroup().getId(), managerId)
                .orElseThrow(() -> new ForbiddenException("Access denied", scheduleId));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new ForbiddenException("Only managers can delete schedules", scheduleId);
        }

        scheduleRepository.delete(schedule);
    }

    public List<ScheduleResponse> getGroupSchedules(UUID groupId, UUID userId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("Access denied", groupId));

        return scheduleRepository.findByGroupIdOrderByStartTimeAsc(groupId).stream()
                .map(s -> new ScheduleResponse(s.getId(), s.getTitle(), s.getStartTime(), s.getEndTime()))
                .collect(Collectors.toList());
    }

    public ScheduleResponse getSchedule(UUID scheduleId, UUID userId) {
        ScheduleInstance schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found", scheduleId));

        groupMemberRepository.findByGroupIdAndUserId(schedule.getGroup().getId(), userId)
                .orElseThrow(() -> new ForbiddenException("Access denied", scheduleId));

        return new ScheduleResponse(schedule.getId(), schedule.getTitle(), schedule.getStartTime(), schedule.getEndTime());
    }
}
