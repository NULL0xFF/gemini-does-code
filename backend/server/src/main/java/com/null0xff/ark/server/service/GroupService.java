package com.null0xff.ark.server.service;

import com.null0xff.ark.server.dto.*;
import com.null0xff.ark.server.entity.*;
import com.null0xff.ark.server.enums.GroupRole;
import com.null0xff.ark.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing groups, rosters, and invitations.
 */
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, InviteCodeRepository inviteCodeRepository, UserRepository userRepository, ScheduleRepository scheduleRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public GroupResponse getGroupDetails(UUID groupId, UUID userId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return new GroupResponse(group.getId(), group.getName(), group.getDescription());
    }

    public List<GroupMemberResponse> getGroupMembers(UUID groupId, UUID userId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

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
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new RuntimeException("Only managers can generate invite codes");
        }

        String codeStr = "ARK-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase() + "-" + UUID.randomUUID().toString().substring(4, 8).toUpperCase();
        
        InviteCode code = new InviteCode();
        code.setCode(codeStr);
        code.setGroup(manager.getGroup());
        code.setCreatedBy(manager.getUser());
        code.setMaxUsage(maxUsage);
        code.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));

        inviteCodeRepository.save(code);

        return new InviteCodeResponse(code.getCode(), code.getMaxUsage(), code.getCurrentUsage(), code.getExpiresAt());
    }

    public List<InviteCodeResponse> getActiveInviteCodes(UUID groupId, UUID managerId) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new RuntimeException("Only managers can view invite codes");
        }

        return inviteCodeRepository.findByGroupId(groupId).stream()
                .filter(code -> code.getExpiresAt().isAfter(LocalDateTime.now()) && (code.getMaxUsage() == null || code.getCurrentUsage() < code.getMaxUsage()))
                .map(code -> new InviteCodeResponse(code.getCode(), code.getMaxUsage(), code.getCurrentUsage(), code.getExpiresAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeInviteCode(UUID groupId, UUID managerId, String codeStr) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new RuntimeException("Only managers can revoke invite codes");
        }

        InviteCode code = inviteCodeRepository.findByCode(codeStr)
                .orElseThrow(() -> new RuntimeException("Code not found"));

        if (!code.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Code does not belong to this group");
        }

        inviteCodeRepository.delete(code);
    }

    @Transactional
    public void joinGroup(UUID userId, String codeStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        InviteCode code = inviteCodeRepository.findByCode(codeStr)
                .orElseThrow(() -> new RuntimeException("Invalid or expired invite code"));

        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invite code has expired");
        }

        if (code.getMaxUsage() != null && code.getCurrentUsage() >= code.getMaxUsage()) {
            throw new RuntimeException("Invite code usage limit reached");
        }

        Optional<GroupMember> existingMember = groupMemberRepository.findByGroupIdAndUserId(code.getGroup().getId(), userId);
        if (existingMember.isPresent()) {
            throw new RuntimeException("You are already a member of this group");
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
    public ScheduleResponse createSchedule(UUID groupId, UUID managerId, String title, LocalDateTime start, LocalDateTime end) {
        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(groupId, managerId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new RuntimeException("Only managers can create schedules");
        }

        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("Invalid schedule time range");
        }

        ScheduleInstance schedule = new ScheduleInstance();
        schedule.setGroup(manager.getGroup());
        schedule.setTitle(title);
        schedule.setStartTime(start);
        schedule.setEndTime(end);

        scheduleRepository.save(schedule);

        return new ScheduleResponse(schedule.getId(), schedule.getTitle(), schedule.getStartTime(), schedule.getEndTime());
    }

    public List<ScheduleResponse> getGroupSchedules(UUID groupId, UUID userId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        return scheduleRepository.findByGroupIdOrderByStartTimeAsc(groupId).stream()
                .map(s -> new ScheduleResponse(s.getId(), s.getTitle(), s.getStartTime(), s.getEndTime()))
                .collect(Collectors.toList());
    }
}
