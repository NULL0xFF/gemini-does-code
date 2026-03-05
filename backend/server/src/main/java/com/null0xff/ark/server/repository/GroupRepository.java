package com.null0xff.ark.server.repository;

import com.null0xff.ark.server.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
}
