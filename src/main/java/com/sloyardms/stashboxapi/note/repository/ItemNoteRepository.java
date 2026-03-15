package com.sloyardms.stashboxapi.note.repository;

import com.sloyardms.stashboxapi.note.model.ItemNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemNoteRepository extends JpaRepository<ItemNote, UUID> {

}
