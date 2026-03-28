package com.sloyardms.stashboxapi.domain.tag.repository;

import com.sloyardms.stashboxapi.domain.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

}
