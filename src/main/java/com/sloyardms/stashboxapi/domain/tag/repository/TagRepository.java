package com.sloyardms.stashboxapi.domain.tag.repository;

import com.sloyardms.stashboxapi.domain.tag.model.Tag;
import com.sloyardms.stashboxapi.domain.tag.projection.TagCountProjection;
import com.sloyardms.stashboxapi.domain.tag.projection.TagDetailProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    @Query(value = """
                SELECT t.id, t.name, t.slug, t.created_at, t.updated_at,
                    COALESCE(tu.item_count, 0) AS item_count, tu.last_used
                FROM tags t
                LEFT JOIN tag_usage tu ON tu.tag_id = t.id
                WHERE t.id = :tagId
                    AND t.group_id = :groupId
                    AND t.user_id = :userId
            """, nativeQuery = true)
    Optional<TagDetailProjection> findTagDetail(@Param("userId") UUID userId, @Param("groupId") UUID groupId, @Param(
            "tagId") UUID tagId);

    @Query(value = """
                SELECT t.id, t.name, t.slug,
                    COALESCE(tu.item_count, 0) AS item_count
                FROM tags t
                LEFT JOIN tag_usage tu ON tu.tag_id = t.id
                WHERE t.group_id = :groupId
                    AND t.user_id = :userId
                    AND (
                            :searchQuery IS NULL
                            OR lower(t.name) ILIKE lower(CONCAT('%', :searchQuery, '%'))
                        )
            """, countQuery = """
                SELECT COUNT(t.id)
                FROM tags t
                WHERE t.group_id = :groupId
                    AND t.user_id = :userId
                    AND (
                            :searchQuery IS NULL
                            OR lower(t.name) ILIKE lower(CONCAT('%', :searchQuery, '%'))
                        )
            """, nativeQuery = true)
    Page<TagCountProjection> findAllTagCount(@Param("userId") UUID userId, @Param("groupId") UUID groupId, @Param(
            "searchQuery") String searchQuery, Pageable pageable);

    Optional<Tag> findByIdAndUserId(UUID id, UUID userId);

    int deleteByIdAndUserIdAndGroupId(UUID id, UUID userId, UUID groupId);

}
