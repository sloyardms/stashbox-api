package com.sloyardms.stashboxapi.tag.repository;

import com.sloyardms.stashboxapi.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

}
