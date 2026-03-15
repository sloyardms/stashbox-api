package com.sloyardms.stashboxapi.stash.model;

import com.sloyardms.stashboxapi.common.model.AuditableEntity;
import com.sloyardms.stashboxapi.note.model.ItemNote;
import com.sloyardms.stashboxapi.tag.model.Tag;
import com.sloyardms.stashboxapi.user.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "stash_items")
public class StashItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "stash_items_user_id_fk"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "group_id", nullable = true, updatable = true,
            foreignKey = @ForeignKey(name = "stash_items_group_id_fk"))
    private ItemGroup group;

    @Column(name = "title")
    @ToString.Include
    private String title;

    @Column(name = "title_normalized")
    private String titleNormalized;

    @Column(name = "url")
    @ToString.Include
    private String url;

    @Column(name = "url_normalized")
    private String urlNormalized;

    @Column(name = "description")
    @ToString.Include
    private String description;

    @Column(name = "image_path")
    @ToString.Include
    private String imagePath;

    @Column(name = "is_favorite", nullable = false)
    @ToString.Include
    private Boolean favorite = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "item_tags",
            joinColumns = @JoinColumn(name = "item_id",
                    foreignKey = @ForeignKey(name = "item_tags_stash_item_id_fk")),
            inverseJoinColumns = @JoinColumn(name = "tag_id",
                    foreignKey = @ForeignKey(name = "item_tags_tag_id_fk")))
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
    private List<ItemNote> notes = new ArrayList<>();

}
