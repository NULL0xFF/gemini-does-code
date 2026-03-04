package com.null0xff.ark.server.repository;

import com.null0xff.ark.server.entity.MemberAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberAvailabilityRepository extends JpaRepository<MemberAvailability, UUID> {
    List<MemberAvailability> findByScheduleId(UUID scheduleId);
    List<MemberAvailability> findByScheduleIdAndUserId(UUID scheduleId, UUID userId);
    void deleteByScheduleIdAndUserId(UUID scheduleId, UUID userId);
}
