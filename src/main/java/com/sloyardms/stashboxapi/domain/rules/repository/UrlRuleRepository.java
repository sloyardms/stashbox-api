package com.sloyardms.stashboxapi.domain.rules.repository;

import com.sloyardms.stashboxapi.domain.rules.model.UrlRule;
import com.sloyardms.stashboxapi.domain.rules.projection.UrlRuleListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UrlRuleRepository extends JpaRepository<UrlRule, UUID> {

    @EntityGraph(value = "UrlRule.withGroup")
    @Query("""
            SELECT ur
            FROM UrlRule ur
            WHERE ur.id = :ruleId
                 AND ur.user.id = :userId
                 AND ur.group.slug = :groupSlug
            """)
    Optional<UrlRule> findWithGroupByIdAndUserIdAndGroupSlug(
            @Param("ruleId") UUID ruleId,
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug);

    @Query("""
            SELECT ur
            FROM UrlRule ur
            WHERE ur.id = :ruleId
                 AND ur.user.id = :userId
                 AND ur.group.slug = :groupSlug
            """)
    Optional<UrlRule> findByIdAndUserIdAndGroupSlug(
            @Param("ruleId") UUID ruleId,
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug);

    @Query("""
            SELECT ur
            FROM UrlRule ur
            WHERE ur.user.id = :userId
                AND ur.group.slug = :groupSlug
                AND ur.domain = :domain
                AND ur.active = true
                ORDER BY ur.priority ASC
            """)
    List<UrlRule> findActiveByDomain(
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug,
            @Param("domain") String domain);

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
    Page<UrlRuleListProjection> search(
            @Param("userId") UUID userId,
            @Param("searchQuery") String searchQuery,
            Pageable pageable);

    @Modifying
    @Query(value = """
            DELETE FROM url_rules ur
            WHERE ur.id = :ruleId
                AND ur.user_id = :userId
                AND ur.group_id = (
                    SELECT id FROM item_groups WHERE slug = :groupSlug AND user_id = :userId
                )
            """, nativeQuery = true)
    int deleteByIdAndUserIdAndGroupSlug(
            @Param("ruleId") UUID id,
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug);

    @Query(value = """
            UPDATE url_rules ur
                SET last_matched_at = NOW()
            FROM item_groups ig
            WHERE ur.group_id = ig.id
                AND ur.id = :ruleId
                AND ur.user_id = :userId
                AND ig.slug = :groupSlug
            """, nativeQuery = true)
    @Modifying(clearAutomatically = true)
    int updateLastMatched(
            @Param("ruleId") UUID ruleId,
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug);

}
