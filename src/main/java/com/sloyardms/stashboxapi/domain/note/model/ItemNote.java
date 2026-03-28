package com.sloyardms.stashboxapi.domain.note.model;

import com.sloyardms.stashboxapi.shared.persistence.AuditableEntity;
import com.sloyardms.stashboxapi.domain.stash.model.StashItem;
import com.sloyardms.stashboxapi.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "item_notes",
        indexes = {
                @Index(name = "item_notes_user_item_idx", columnList = "user_id,item_id")
        }
)
public class ItemNote extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "item_notes_user_id_fk"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "item_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "item_notes_item_id_fk"))
    private StashItem item;

    @Column(name = "content")
    @ToString.Include
    private String content;

    @Column(name = "position", nullable = false)
    @ToString.Include
    private Integer position = 0;

    @Column(name = "is_pinned", nullable = false)
    @ToString.Include
    private Boolean pinned = false;

    @Column(name = "is_draft", nullable = false)
    @ToString.Include
    private Boolean draft = true;

}
