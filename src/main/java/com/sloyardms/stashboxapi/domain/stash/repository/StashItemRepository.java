package com.sloyardms.stashboxapi.domain.stash.repository;

import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StashItemRepository extends JpaRepository<StashItem, UUID> {

}
