package com.null0xff.ark.server.repository;

import com.null0xff.ark.server.entity.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link PartyMember} join-table entities.
 *
 * <p>Supports party roster lookups, membership checks, and cascading deletion when a
 * member is removed from a group.
 */
@Repository
public interface PartyMemberRepository extends JpaRepository<PartyMember, UUID> {

  /**
   * Returns all member records for the given party.
   *
   * @param partyId the party's unique identifier
   * @return a list of party memberships; empty if the party has no members
   */
  List<PartyMember> findByPartyId(UUID partyId);

  /**
   * Returns the membership record for a specific user in a specific party.
   *
   * @param partyId the party's unique identifier
   * @param userId  the user's unique identifier
   * @return an {@link Optional} containing the record, or empty if the user has not joined
   */
  Optional<PartyMember> findByPartyIdAndUserId(UUID partyId, UUID userId);

  /**
   * Returns the number of members currently in the given party.
   *
   * @param partyId the party's unique identifier
   * @return the member count
   */
  int countByPartyId(UUID partyId);

  /**
   * Deletes all party memberships for a user across every party in the given group.
   *
   * <p>Used during member removal to clean up all of their party slots within a group
   * in a single query.
   *
   * @param userId  the user's unique identifier
   * @param groupId the group's unique identifier
   */
  @Modifying
  @Query("DELETE FROM PartyMember pm WHERE pm.user.id = :userId AND pm.party.schedule.group.id = :groupId")
  void deleteByUserIdAndGroupId(UUID userId, UUID groupId);
}
