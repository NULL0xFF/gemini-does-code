package com.null0xff.ark.server.repository;

import com.null0xff.ark.server.entity.ScheduleInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleInstance, UUID> {
    List<ScheduleInstance> findByGroupIdOrderByStartTimeAsc(UUID groupId);
}
