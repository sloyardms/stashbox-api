package com.sloyardms.stashboxapi.domain.stash.repository;

import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.stash.projection.ItemGroupWithCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ItemGroupRepository extends JpaRepository<ItemGroup, UUID> {

    Optional<ItemGroup> findBySlugAndUserId(String slug, UUID userId);

    boolean existsBySlugAndUserId(String slug, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(MAX(ig.position),0) FROM ItemGroup ig WHERE ig.user.id = :userId")
    int findMaxPositionByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE ItemGroup ig SET ig.defaultGroup = false WHERE ig.defaultGroup = true AND ig.user.id =:userId")
    void clearDefaultGroup(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE ItemGroup ig SET ig.defaultGroup = true WHERE ig.slug = :slug AND ig.user.id = :userId")
    void setDefaultGroup(@Param("slug") String slug, @Param("userId") UUID userId);

    @Query(
            value = """
        SELECT ig.id AS id,
               ig.name AS name,
               ig.description AS description,
               ig.slug AS slug,
               ig.icon AS icon,
               ig.defaultGroup AS defaultGroup,
               ig.position AS position,
               COUNT(si.id) AS itemCount
        FROM ItemGroup ig
        LEFT JOIN StashItem si ON si.group = ig
        WHERE ig.user.id = :userId
        GROUP BY ig.id, ig.name, ig.slug, ig.icon, ig.defaultGroup, ig.position
        ORDER BY ig.position ASC
        """, countQuery = """
        SELECT COUNT(ig)
        FROM ItemGroup ig
        WHERE ig.user.id = :userId
        """
    )
    Page<ItemGroupWithCount> findAllWithItemCountByUserId(UUID userId, Pageable pageable);
}
