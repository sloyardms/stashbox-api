package com.sloyardms.stashboxapi.domain.stash.repository;

import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.stash.projection.StashItemSearchProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StashItemRepository extends JpaRepository<StashItem, UUID> {

    boolean existsByGroupIdAndTitleNormalized(UUID groupId,String title);

    boolean existsByGroupIdAndTitleNormalizedAndIdNot(UUID groupId, String title, UUID excludedId);

    boolean existsByGroupIdAndUrlNormalized(UUID groupId,String url);

    boolean existsByGroupIdAndUrlNormalizedAndIdNot(UUID groupId, String url, UUID excludedId);

    Optional<StashItem> findByIdAndUserId(UUID id, UUID userId);

    Optional<StashItem> findByIdAndUserIdAndGroupSlug(UUID slug, UUID userId, String groupSlug);

    @Query(value = """
        SELECT si.id, si.title, si.url, si.description, si.image_path as imagePath,
               si.is_favorite as favorite, si.created_at as createdAt
        FROM stash_items si
        JOIN item_groups ig ON ig.id = si.group_id
        WHERE si.user_id = :userId
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
        WHERE si.user_id = :userId AND ig.slug = :groupSlug AND si.deleted_at IS NULL
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
            WHERE si.user_id = :userId
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
        WHERE si.user_id = :userId AND ig.slug = :groupSlug AND si.deleted_at IS NULL
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

    List<StashItem> findAllByIdInAndUserId(List<UUID> ids, UUID userId);

    @Modifying
    @Query("""
        UPDATE StashItem si
        SET si.favorite = CASE
            WHEN si.favorite = TRUE THEN FALSE
            ELSE TRUE
        END
        WHERE
            si.user.id = :userId AND
            si.group.slug = :groupSlug AND
            si.id IN :stashItemIds
    """)
    int toggleFavoriteMany(
            @Param("userId") UUID userId,
            @Param("groupSlug") String groupSlug,
            @Param("stashItemIds") List<UUID> stashItemIds
    );

    @Query(value = """
        SELECT * FROM (
            SELECT si.id, si.title, si.url, si.description, si.image_path as imagePath,
               si.is_favorite as favorite, si.created_at as createdAt,
               si.deleted_at as deletedAt,
               ts_rank(si.search_vector, to_tsquery('simple', :tsQuery)) AS search_rank
            FROM stash_items si
            WHERE si.user_id = :userId
              AND si.deleted_at IS NOT NULL
              AND si.search_vector @@ to_tsquery('simple', :tsQuery)
        ) si
    """,countQuery = """
        SELECT count(*) FROM stash_items si
        WHERE si.user_id = :userId
          AND si.deleted_at IS NOT NULL
          AND si.search_vector @@ to_tsquery('simple', :tsQuery)
    """,
            nativeQuery = true)
    Page<StashItemSearchProjection> searchInDeleted(@Param("userId") UUID userId,
                                                   @Param("tsQuery") String tsQuery,
                                                   Pageable pageable);

    @Query(value = """
        SELECT si.id, si.title, si.url, si.description, si.image_path as imagePath,
               si.is_favorite as favorite, si.created_at as createdAt,
               si.deleted_at as deletedAt
        FROM stash_items si
        JOIN item_groups ig ON ig.id = si.group_id
        WHERE si.user_id = :userId AND si.deleted_at IS NOT NULL
    """,
                countQuery = """
        SELECT count(*) FROM stash_items si
        WHERE si.user_id = :userId AND si.deleted_at IS NOT NULL
    """,
            nativeQuery = true)
    Page<StashItemSearchProjection> listInDeleted(@Param("userId") UUID userId, Pageable pageable);

    List<StashItem> findAllByUserIdAndDeletedAtNotNull(UUID userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM stash_items WHERE user_id = :userId AND deleted_at IS NOT NULL", nativeQuery = true)
    long emptyTrash(@Param("userId") UUID userId);

    long countByUserIdAndDeletedAtIsNotNull(UUID userId);

    @Modifying
    @Query(value = "UPDATE stash_items SET deleted_at = NULL WHERE id = :itemId AND user_id = :userId", nativeQuery = true)
    long restore(@Param("itemId") UUID itemId, @Param("userId") UUID userId);

}
