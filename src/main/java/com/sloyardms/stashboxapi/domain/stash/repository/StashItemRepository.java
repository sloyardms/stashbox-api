package com.sloyardms.stashboxapi.domain.stash.repository;

import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
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
