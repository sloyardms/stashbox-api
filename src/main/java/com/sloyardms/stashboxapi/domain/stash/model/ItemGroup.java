package com.sloyardms.stashboxapi.domain.stash.model;

import com.sloyardms.stashboxapi.shared.persistence.AuditableEntity;
import com.sloyardms.stashboxapi.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "item_groups",
        uniqueConstraints = {
                @UniqueConstraint(name = "item_groups_slug_unique", columnNames = {"user_id", "slug"})
        })
public class ItemGroup extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "item_groups_user_id_fk"))
    private User user;

    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    @Column(name = "slug", nullable = false)
    @ToString.Include
    private String slug;

    @Column(name = "description")
    @ToString.Include
    private String description;

    @Column(name = "settings", columnDefinition = "jsonb not null default '{}'::jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private ItemGroupSettings settings = new ItemGroupSettings();

    @Column(name = "position", nullable = false)
    @ToString.Include
    private Integer position = 0;

}
