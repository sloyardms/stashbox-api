package com.sloyardms.stashboxapi.domain.note.repository;

import com.sloyardms.stashboxapi.domain.note.model.NoteFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NoteFileRepository extends JpaRepository<NoteFile, UUID> {

}
