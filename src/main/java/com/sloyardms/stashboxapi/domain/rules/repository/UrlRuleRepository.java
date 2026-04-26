package com.sloyardms.stashboxapi.domain.rules.repository;

import com.sloyardms.stashboxapi.domain.rules.model.UrlRule;
import com.sloyardms.stashboxapi.domain.rules.projection.UrlRuleListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UrlRuleRepository extends JpaRepository<UrlRule, UUID> {

    @EntityGraph(value = "UrlRule.withGroup")
    Optional<UrlRule> findWithGroupByIdAndUserIdAndGroupId(UUID id, UUID userId, UUID groupId);

    Optional<UrlRule> findByIdAndUserIdAndGroupId(UUID id, UUID userId, UUID groupId);

    @Query("""
            SELECT ur
            FROM UrlRule ur
            WHERE ur.user.id = :userId
                AND ur.group.id = :groupId
                AND ur.domain = :domain
                AND ur.active = true
                ORDER BY ur.priority ASC
            """)
    List<UrlRule> findActiveByDomain(UUID userId, UUID groupId, String domain);

    @Query(value = """
            SELECT
                ur.id,
                ur.name,
                ur.domain,
                ur.is_active AS active,
                ur.priority,
                ur.last_matched_at AS lastMatchedAt,
                ur.created_at AS createdAt,
                ur.updated_at AS updatedAt,
                ig.id AS groupId,
                ig.name AS groupName
            FROM url_rules ur
            LEFT JOIN item_groups ig ON ur.group_id = ig.id
            WHERE ur.user_id = :userId
                AND (
                    :searchQuery IS NULL
                    OR ur.name ILIKE CONCAT('%', :searchQuery, '%')
                    OR ur.domain ILIKE CONCAT ('%', :searchQuery, '%')
                    )
            """, countQuery = """
            SELECT COUNT(*)
            FROM url_rules ur
            WHERE ur.user_id = :userId
                AND (
                    :searchQuery IS NULL
                    OR ur.name ILIKE CONCAT('%', :searchQuery, '%')
                    OR ur.domain ILIKE CONCAT ('%', :searchQuery, '%')
                    )
            """, nativeQuery = true)
    Page<UrlRuleListProjection> search(UUID userId, String searchQuery, Pageable pageable);

    int deleteByIdAndUserIdAndGroupId(UUID id, UUID userId, UUID groupId);

    @Query(value = """
            UPDATE url_rules
            SET last_matched_at = NOW()
            WHERE id = :id
                AND user_id = :userId
                AND group_id = :groupId
            """, nativeQuery = true)
    @Modifying(clearAutomatically = true)
    int updateLastMatched(UUID id, UUID userId, UUID groupId);

}
