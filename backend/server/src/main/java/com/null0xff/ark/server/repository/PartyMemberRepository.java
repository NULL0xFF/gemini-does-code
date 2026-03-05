package com.null0xff.ark.server.repository;

import com.null0xff.ark.server.entity.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PartyMemberRepository extends JpaRepository<PartyMember, UUID> {
    List<PartyMember> findByPartyId(UUID partyId);
    Optional<PartyMember> findByPartyIdAndUserId(UUID partyId, UUID userId);
    int countByPartyId(UUID partyId);

    @Modifying
    @Query("DELETE FROM PartyMember pm WHERE pm.user.id = :userId AND pm.party.schedule.group.id = :groupId")
    void deleteByUserIdAndGroupId(UUID userId, UUID groupId);
}
