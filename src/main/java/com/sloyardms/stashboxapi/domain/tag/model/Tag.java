package com.sloyardms.stashboxapi.domain.tag.model;

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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "tags_slug_unique", columnNames = {"user_id", "slug"})
        })
public class Tag extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "tags_user_id_fk"))
    private User user;

    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    @Column(name = "slug", nullable = false)
    @ToString.Include
    private String slug;

}
