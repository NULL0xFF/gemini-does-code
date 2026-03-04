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
