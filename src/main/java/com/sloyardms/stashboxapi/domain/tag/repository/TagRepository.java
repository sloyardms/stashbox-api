package com.sloyardms.stashboxapi.domain.tag.repository;

import com.sloyardms.stashboxapi.domain.tag.model.Tag;
import com.sloyardms.stashboxapi.domain.tag.projection.TagCountProjection;
import com.sloyardms.stashboxapi.domain.tag.projection.TagDetailProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findBySlugAndUserIdAndGroupSlug(String slug, UUID userId, String groupSlug);

    @Query(value = """
                SELECT t.id, t.name, t.slug, t.created_at, t.updated_at,
                    COALESCE(tu.item_count, 0) AS item_count, tu.last_used
                FROM tags t
                INNER JOIN item_groups ig ON ig.id = t.group_id
                LEFT JOIN tag_usage tu ON t.id = tu.tag_id
                WHERE t.slug = :tagSlug
                    AND ig.slug = :groupSlug
                    AND t.user_id = :userId
            """, nativeQuery = true)
    Optional<TagDetailProjection> findTagDetail(
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug,
            @Param("tagSlug") String tagSlug);

    @Query(value = """
                SELECT t.id, t.name, t.slug, t.created_at, t.updated_at,
                    COALESCE(tu.item_count, 0) AS item_count,
                    tu.last_used as last_used
                FROM tags t
                INNER JOIN item_groups ig ON ig.id = t.group_id
                LEFT JOIN tag_usage tu ON tu.tag_id = t.id
                WHERE ig.slug = :groupSlug
                    AND t.user_id = :userId
                    AND (
                            :searchQuery IS NULL
                            OR t.name ILIKE CONCAT('%', :searchQuery, '%')
                        )
            """, countQuery = """
                SELECT COUNT(t.id)
                FROM tags t
                INNER JOIN item_groups ig ON ig.id = t.group_id
                WHERE ig.slug = :groupSlug
                    AND t.user_id = :userId
                    AND (
                            :searchQuery IS NULL
                            OR t.name ILIKE CONCAT('%', :searchQuery, '%')
                        )
            """, nativeQuery = true)
    Page<TagCountProjection> findAllTagCount(
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug,
            @Param("searchQuery") String searchQuery, Pageable pageable);

    @Modifying
    @Query(value = """
                DELETE FROM tags t
                WHERE t.slug = :tagSlug
                    AND t.user_id = :userId
                    AND t.group_id = (
                        SELECT id FROM item_groups WHERE slug = :groupSlug AND user_id = :userId
                    )
            """, nativeQuery = true)
    int deleteBySlugAndUserIdAndGroupSlug(
            @Param("tagSlug") String tagSlug,
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug);

    @Query("SELECT t FROM Tag t WHERE t.user.id = :userId AND t.group.id = :groupId AND t.slug IN :slugs")
    List<Tag> findAllByUserIdAndGroupIdAndSlugIn(UUID userId, UUID groupId, Set<String> slugs);

    @Query(value = """
            SELECT t.id, t.name, t.slug,
                COALESCE(tu.item_count, 0) AS item_count
            FROM item_tags it
            JOIN tags t ON t.id = it.tag_id
            LEFT JOIN tag_usage tu ON tu.tag_id = t.id
            WHERE it.item_id = :stashItemId
            """, nativeQuery = true)
    List<TagCountProjection> findTagsWithCountForStashItem(
            @Param("stashItemId") UUID stashItemId);

    @Modifying
    @Query("""
        DELETE FROM Tag t
        WHERE t.id IN :tagIds
          AND t.user.id = :userId
          AND t.group.slug = :groupSlug
    """)
    long deleteMany(
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug,
            @Param("tagIds") List<UUID> tagIds
    );

    /**
     * Creates a tag_usage row for any tag that is missing one (e.g. drift, or a tag
     * created before the tag_usage trigger existed). Safe to run repeatedly.
     *
     * @return number of rows inserted
     */
    @Modifying
    @Query(value = """
        INSERT INTO tag_usage (tag_id, item_count, last_used)
        SELECT t.id, 0, now()
        FROM tags t
        WHERE NOT EXISTS (SELECT 1 FROM tag_usage tu WHERE tu.tag_id = t.id)
    """, nativeQuery = true)
    int backfillMissingTagUsageRows();

    /**
     * Recomputes tag_usage.item_count from the source of truth (item_tags joined to
     * non-soft-deleted stash_items), only touching rows that actually drifted.
     *
     * @return number of rows corrected
     */
    @Modifying
    @Query(value = """
        UPDATE tag_usage tu
        SET item_count = sub.cnt
        FROM (
            SELECT t.id AS tag_id,
                   COUNT(si.id) FILTER (WHERE si.deleted_at IS NULL) AS cnt
            FROM tags t
            LEFT JOIN item_tags it ON it.tag_id = t.id
            LEFT JOIN stash_items si ON si.id = it.item_id
            GROUP BY t.id
        ) sub
        WHERE tu.tag_id = sub.tag_id
          AND tu.item_count IS DISTINCT FROM sub.cnt
    """, nativeQuery = true)
    int reconcileTagUsageCounts();

}
