package com.sloyardms.stashboxapi.domain.rules.model;

import com.sloyardms.stashboxapi.domain.stash.model.ItemGroup;
import com.sloyardms.stashboxapi.domain.user.model.User;
import com.sloyardms.stashboxapi.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@NamedEntityGraph(
        name = "UrlRule.withGroup",
        attributeNodes = @NamedAttributeNode("group")
)
@Table(name = "url_rules",
        uniqueConstraints = {
                @UniqueConstraint(name = "url_rules_user_name_unique", columnNames = {"user_id", "group_id", "name"}),
                @UniqueConstraint(name = "url_rules_user_url_pattern_unique", columnNames = {"user_id", "group_id",
                        "url_pattern"})
        })
public class UrlRule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "url_rules_user_id_fk"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "group_id", nullable = false,
            foreignKey = @ForeignKey(name = "url_rules_group_id_fk"))
    private ItemGroup group;

    @Column(name = "name", nullable = false, length = 50)
    @ToString.Include
    private String name;

    @Column(name = "description", nullable = true, length = 255)
    @ToString.Include
    private String description;

    @Column(name = "domain", nullable = false, length = 100)
    @ToString.Include
    private String domain;

    @Column(name = "url_pattern", nullable = false, length = 2048)
    @ToString.Include
    private String urlPattern;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transforms", columnDefinition = "jsonb", nullable = false)
    private List<Transform> transforms = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @ToString.Include
    private Boolean active = true;

    @Column(name = "priority", nullable = false)
    @ToString.Include
    private Integer priority = 100;

    @Column(name = "last_matched_at")
    @ToString.Include
    private Instant lastMatchedAt;

}
