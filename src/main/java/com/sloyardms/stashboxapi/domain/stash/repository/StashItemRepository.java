package com.sloyardms.stashboxapi.domain.stash.repository;

import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.projection.StashItemSearchProjection;
import com.sloyardms.stashboxapi.domain.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StashItemRepository extends JpaRepository<StashItem, UUID> {

    boolean existsByGroupIdAndTitleNormalized(UUID groupId,String title);

    boolean existsByGroupIdAndTitleNormalizedAndIdNot(UUID groupId, String title, UUID excludedId);

    boolean existsByGroupIdAndUrlNormalized(UUID groupId,String url);

    boolean existsByGroupIdAndUrlNormalizedAndIdNot(UUID groupId, String url, UUID excludedId);

    Optional<StashItem> findByIdAndUserIdAndGroupSlug(UUID id, UUID userId, String groupSlug);

    Page<StashItem> findAllByUserIdAndGroupSlug(UUID userId, String groupSlug, Pageable pageable);

    @Query(value = """
        SELECT si.id, si.title, si.url, si.description, si.image_path as imagePath,
               si.is_favorite as favorite, si.created_at as createdAt
        FROM stash_items si
        JOIN item_groups ig ON ig.id = si.group_id
        WHERE ig.user_id = :userId
          AND ig.slug = :groupSlug
          AND si.deleted_at IS NULL
          AND (
            :tagSlugsCsv IS NULL
            OR (
              SELECT COUNT(DISTINCT t.slug)
              FROM item_tags it JOIN tags t ON t.id = it.tag_id
              WHERE it.item_id = si.id AND t.slug = ANY(string_to_array(:tagSlugsCsv, ','))
            ) = cardinality(string_to_array(:tagSlugsCsv, ','))
          )
    """,
            countQuery = """
        SELECT count(*) FROM stash_items si
        JOIN item_groups ig ON ig.id = si.group_id
        WHERE ig.user_id = :userId AND ig.slug = :groupSlug AND si.deleted_at IS NULL
          AND (
            :tagSlugsCsv IS NULL
            OR (
              SELECT COUNT(DISTINCT t.slug)
              FROM item_tags it JOIN tags t ON t.id = it.tag_id
              WHERE it.item_id = si.id AND t.slug = ANY(string_to_array(:tagSlugsCsv, ','))
            ) = cardinality(string_to_array(:tagSlugsCsv, ','))
          )
    """,
            nativeQuery = true)
    Page<StashItemSearchProjection> listInGroup(@Param("userId") UUID userId,
                                                @Param("groupSlug") String groupSlug,
                                                @Param("tagSlugsCsv") String tagSlugsCsv,
                                                Pageable pageable);

    @Query(value = """
        SELECT * FROM (
            SELECT si.id, si.title, si.url, si.description, si.image_path as imagePath,
               si.is_favorite, si.created_at as createdAt,
               ts_rank(si.search_vector, to_tsquery('simple', :tsQuery)) AS search_rank
                FROM stash_items si
                JOIN item_groups ig ON ig.id = si.group_id
                WHERE ig.user_id = :userId
          AND ig.slug = :groupSlug
          AND si.deleted_at IS NULL
          AND si.search_vector @@ to_tsquery('simple', :tsQuery)
          AND (
            :tagSlugsCsv IS NULL
            OR (
              SELECT COUNT(DISTINCT t.slug)
              FROM item_tags it JOIN tags t ON t.id = it.tag_id
              WHERE it.item_id = si.id AND t.slug = ANY(string_to_array(:tagSlugsCsv, ','))
            ) = cardinality(string_to_array(:tagSlugsCsv, ','))
          )
        ) si
    """,
            countQuery = """
        SELECT count(*) FROM stash_items si
        JOIN item_groups ig ON ig.id = si.group_id
        WHERE ig.user_id = :userId AND ig.slug = :groupSlug AND si.deleted_at IS NULL
          AND si.search_vector @@ to_tsquery('simple', :tsQuery)
          AND (
            :tagSlugsCsv IS NULL
            OR (
              SELECT COUNT(DISTINCT t.slug)
              FROM item_tags it JOIN tags t ON t.id = it.tag_id
              WHERE it.item_id = si.id AND t.slug = ANY(string_to_array(:tagSlugsCsv, ','))
            ) = cardinality(string_to_array(:tagSlugsCsv, ','))
          )
    """,
            nativeQuery = true)
    Page<StashItemSearchProjection> searchInGroup(@Param("userId") UUID userId,
                                                  @Param("groupSlug") String groupSlug,
                                                  @Param("tsQuery") String tsQuery,
                                                  @Param("tagSlugsCsv") String tagSlugsCsv,
                                                  Pageable pageable);

    @Modifying
    @Query("""
        UPDATE StashItem si
        SET si.favorite = CASE
            WHEN si.favorite = TRUE THEN FALSE
            ELSE TRUE
        END
        WHERE
            si.user.id = :userId AND
            si.group.slug  = :groupSlug AND
            si.id = :stashItemId
    """)
    int toggleFavorite(
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug,
            @Param("stashItemId") UUID stashItemId);
}
