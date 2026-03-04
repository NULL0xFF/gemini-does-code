package com.null0xff.ark.server.service;

import com.null0xff.ark.server.dto.AvailabilityBlock;
import com.null0xff.ark.server.dto.AvailabilityResponse;
import com.null0xff.ark.server.entity.GroupMember;
import com.null0xff.ark.server.entity.MemberAvailability;
import com.null0xff.ark.server.entity.ScheduleInstance;
import com.null0xff.ark.server.entity.User;
import com.null0xff.ark.server.repository.GroupMemberRepository;
import com.null0xff.ark.server.repository.MemberAvailabilityRepository;
import com.null0xff.ark.server.repository.ScheduleRepository;
import com.null0xff.ark.server.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing member availability and aggregated heatmaps.
 */
@Service
public class AvailabilityService {

    private final MemberAvailabilityRepository availabilityRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public AvailabilityService(MemberAvailabilityRepository availabilityRepository, ScheduleRepository scheduleRepository, UserRepository userRepository, GroupMemberRepository groupMemberRepository) {
        this.availabilityRepository = availabilityRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional
    public void updateAvailability(UUID scheduleId, UUID userId, List<AvailabilityBlock> blocks) {
        ScheduleInstance schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        groupMemberRepository.findByGroupIdAndUserId(schedule.getGroup().getId(), userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        availabilityRepository.deleteByScheduleIdAndUserId(scheduleId, userId);

        List<MemberAvailability> newAvailabilities = blocks.stream().map(block -> {
            MemberAvailability availability = new MemberAvailability();
            availability.setSchedule(schedule);
            availability.setUser(user);
            availability.setStartTime(block.getStart());
            availability.setEndTime(block.getEnd());
            return availability;
        }).collect(Collectors.toList());

        availabilityRepository.saveAll(newAvailabilities);
    }

    public List<AvailabilityResponse> getAggregatedAvailability(UUID scheduleId, UUID userId) {
        ScheduleInstance schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        groupMemberRepository.findByGroupIdAndUserId(schedule.getGroup().getId(), userId)
                .orElseThrow(() -> new RuntimeException("Access denied"));

        List<MemberAvailability> availabilities = availabilityRepository.findByScheduleId(scheduleId);

        Map<UUID, List<MemberAvailability>> byUser = availabilities.stream()
                .collect(Collectors.groupingBy(a -> a.getUser().getId()));

        return byUser.entrySet().stream()
                .map(entry -> {
                    List<AvailabilityBlock> blocks = entry.getValue().stream().map(a -> {
                        AvailabilityBlock block = new AvailabilityBlock();
                        block.setStart(a.getStartTime());
                        block.setEnd(a.getEndTime());
                        return block;
                    }).collect(Collectors.toList());
                    return new AvailabilityResponse(entry.getKey(), blocks);
                })
                .collect(Collectors.toList());
    }
}
