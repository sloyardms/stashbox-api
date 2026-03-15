package com.sloyardms.stashboxapi.stash.repository;

import com.sloyardms.stashboxapi.stash.model.ItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemGroupRepository extends JpaRepository<ItemGroup, UUID> {

}
