package com.null0xff.ark.server.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a specific raid party organized within a schedule.
 */
@Entity
@Table(name = "parties")
@Getter
@Setter
@NoArgsConstructor
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleInstance schedule;

    @Column(nullable = false)
    private String title;

    @Column(name = "raid_type", nullable = false)
    private String raidType;

    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyMember> members = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
