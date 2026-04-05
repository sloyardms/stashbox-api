package com.sloyardms.stashboxapi.domain.stash.repository;

import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ItemGroupRepository extends JpaRepository<ItemGroup, UUID> {

    Optional<ItemGroup> findByIdAndUserId(UUID id, UUID userId);

    Page<ItemGroup> findAllByUserId(UUID userId, Pageable pageable);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(MAX(ig.position),0) FROM ItemGroup ig WHERE ig.user.id = :userId")
    int findMaxPositionByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE ItemGroup ig SET ig.defaultGroup = false WHERE ig.defaultGroup = true AND ig.user.id =:userId")
    void clearDefaultGroup(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE ItemGroup ig SET ig.defaultGroup = true WHERE ig.id = :id AND ig.user.id = :userId")
    void setDefaultGroup(@Param("id") UUID id, @Param("userId") UUID userId);

}
