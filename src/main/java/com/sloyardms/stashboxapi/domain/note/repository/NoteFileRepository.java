package com.sloyardms.stashboxapi.domain.note.repository;

import com.sloyardms.stashboxapi.domain.note.model.NoteFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteFileRepository extends JpaRepository<NoteFile, UUID> {

    List<NoteFile> findAllByIdInAndUserIdAndNoteId(List<UUID> ids, UUID userId, UUID noteId);

    @Query("""
        SELECT COALESCE(MAX(nf.displayOrder), -1)
        FROM NoteFile nf
        WHERE nf.note.id = :noteId
    """)
    Optional<Integer> findMaxDisplayOrderByNoteId(UUID noteId);
}
