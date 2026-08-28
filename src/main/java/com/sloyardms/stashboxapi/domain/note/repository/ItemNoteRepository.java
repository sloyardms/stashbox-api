package com.sloyardms.stashboxapi.domain.note.repository;

import com.sloyardms.stashboxapi.domain.note.model.ItemNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface ItemNoteRepository extends JpaRepository<ItemNote, UUID> {

    @Query("""
        SELECT n FROM ItemNote n
        WHERE n.user.id = :userId AND n.item.id = :itemId
        ORDER BY n.pinned DESC, n.createdAt DESC
        """)
    Page<ItemNote> findByUserIdAndGroupSlugItemId(
            @Param("userId") UUID userId,
            @Param("itemId") UUID itemId,
            Pageable pageable);

    Optional<ItemNote> findByIdAndUserIdAndItemId(UUID id, UUID userId, UUID itemId);

}
