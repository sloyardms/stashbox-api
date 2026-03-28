package com.sloyardms.stashboxapi.domain.note.model;

import com.sloyardms.stashboxapi.shared.persistence.AuditableEntity;
import com.sloyardms.stashboxapi.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "note_files",
        indexes = {
                @Index(name = "note_files_user_note_idx", columnList = "user_id,note_id")
        }
)
public class NoteFile extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "note_files_user_id_fk"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "note_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "note_files_item_note_id_fk"))
    private ItemNote note;

    @Column(name = "original_filename", nullable = false)
    @ToString.Include
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    @ToString.Include
    private String storedFilename;

    @Column(name = "file_path", nullable = false)
    @ToString.Include
    private String filePath;

    @Column(name = "mime_type", nullable = false)
    @ToString.Include
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    @ToString.Include
    private Long fileSize;

    @Column(name = "file_extension", nullable = false)
    @ToString.Include
    private String fileExtension;

    @Column(name = "upload_status", nullable = false, columnDefinition = "upload_status_enum")
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private UploadStatus status = UploadStatus.PENDING;

    @Column(name = "display_order", nullable = false)
    @ToString.Include
    private Integer displayOrder = 0;

}
