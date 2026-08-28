package com.sloyardms.stashboxapi.domain.note.service;

import com.sloyardms.stashboxapi.domain.note.repository.NoteFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFileCleanupService {

    private final NoteFileRepository noteFileRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteOrphanedRows(List<UUID> noteFileIds) {
        noteFileRepository.deleteAllById(noteFileIds);
    }

}