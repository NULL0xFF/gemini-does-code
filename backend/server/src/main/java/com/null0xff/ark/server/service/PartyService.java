package com.null0xff.ark.server.service;

import com.null0xff.ark.server.dto.PartyRequest;
import com.null0xff.ark.server.dto.PartyResponse;
import com.null0xff.ark.server.entity.GroupMember;
import com.null0xff.ark.server.entity.Party;
import com.null0xff.ark.server.entity.PartyMember;
import com.null0xff.ark.server.entity.ScheduleInstance;
import com.null0xff.ark.server.entity.User;
import com.null0xff.ark.server.enums.GroupRole;
import com.null0xff.ark.server.repository.GroupMemberRepository;
import com.null0xff.ark.server.repository.PartyMemberRepository;
import com.null0xff.ark.server.repository.PartyRepository;
import com.null0xff.ark.server.repository.ScheduleRepository;
import com.null0xff.ark.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing raid parties within schedules.
 */
@Service
public class PartyService {

    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final ScheduleRepository scheduleRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public PartyService(PartyRepository partyRepository, PartyMemberRepository partyMemberRepository, ScheduleRepository scheduleRepository, GroupMemberRepository groupMemberRepository, UserRepository userRepository) {
        this.partyRepository = partyRepository;
        this.partyMemberRepository = partyMemberRepository;
        this.scheduleRepository = scheduleRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PartyResponse createParty(UUID scheduleId, UUID managerId, PartyRequest request) {
        ScheduleInstance schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        GroupMember manager = groupMemberRepository.findByGroupIdAndUserId(schedule.getGroup().getId(), managerId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        if (manager.getRole() != GroupRole.MANAGER) {
            throw new RuntimeException("Only managers can create parties");
        }

        Party party = new Party();
        party.setSchedule(schedule);
        party.setTitle(request.getTitle());
        party.setRaidType(request.getRaidType());
        party.setMaxMembers(request.getMaxMembers());
        party.setStartTime(request.getStartTime());

        partyRepository.save(party);

        return new PartyResponse(party.getId(), scheduleId, party.getTitle(), party.getRaidType(), 0, party.getMaxMembers(), party.getStartTime(), List.of());
    }

    public List<PartyResponse> getScheduleParties(UUID scheduleId, UUID userId) {
        ScheduleInstance schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        groupMemberRepository.findByGroupIdAndUserId(schedule.getGroup().getId(), userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        return partyRepository.findByScheduleIdOrderByStartTimeAsc(scheduleId).stream().map(party -> {
            List<PartyMember> members = partyMemberRepository.findByPartyId(party.getId());
            List<String> memberNames = members.stream()
                    .map(pm -> {
                        String nickname = pm.getUser().getNickname();
                        return (nickname != null && !nickname.isEmpty()) ? nickname : pm.getUser().getUsername();
                    })
                    .collect(Collectors.toList());
            return new PartyResponse(party.getId(), scheduleId, party.getTitle(), party.getRaidType(), members.size(), party.getMaxMembers(), party.getStartTime(), memberNames);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void joinParty(UUID partyId, UUID userId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found"));

        groupMemberRepository.findByGroupIdAndUserId(party.getSchedule().getGroup().getId(), userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        int currentMembers = partyMemberRepository.countByPartyId(partyId);
        if (currentMembers >= party.getMaxMembers()) {
            throw new RuntimeException("Party is full");
        }

        Optional<PartyMember> existing = partyMemberRepository.findByPartyIdAndUserId(partyId, userId);
        if (existing.isPresent()) {
            throw new RuntimeException("You are already in this party");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PartyMember member = new PartyMember();
        member.setParty(party);
        member.setUser(user);
        partyMemberRepository.save(member);
    }

    @Transactional
    public void leaveParty(UUID partyId, UUID userId) {
        PartyMember member = partyMemberRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new RuntimeException("You are not in this party"));
        
        partyMemberRepository.delete(member);
    }
}
