package com.sloyardms.stashboxapi.domain.note.repository;

import com.sloyardms.stashboxapi.domain.note.model.ItemNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemNoteRepository extends JpaRepository<ItemNote, UUID> {

}
