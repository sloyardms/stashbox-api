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
                SELECT t.id, t.name, t.slug,
                    COALESCE(tu.item_count, 0) AS item_count
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

}
