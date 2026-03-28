package com.sloyardms.stashboxapi.domain.user.model;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_filters",
        uniqueConstraints = {
                @UniqueConstraint(name = "user_filters_user_name_unique", columnNames = {"user_id", "name"}),
                @UniqueConstraint(name = "user_filters_user_url_pattern_unique", columnNames = {"user_id",
                        "url_pattern"})
        })
public class UserFilter extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "user_filters_user_id_fk"))
    private User user;

    @Column(name = "name", nullable = false)
    @ToString.Include
    private String name;

    @Column(name = "domain", nullable = false)
    @ToString.Include
    private String domain;

    @Column(name = "url_pattern", nullable = false)
    @ToString.Include
    private String urlPattern;

    @Column(name = "extraction_pattern", nullable = false)
    @ToString.Include
    private String extractionPattern;

    @Column(name = "extraction_group", nullable = false)
    @PositiveOrZero
    @ToString.Include
    private Integer extractionGroup = 1;

    @Column(name = "title_template")
    @ToString.Include
    private String titleTemplate;

    @Column(name = "is_active", nullable = false)
    @ToString.Include
    private Boolean active = true;

    @Column(name = "priority", nullable = false)
    @ToString.Include
    @PositiveOrZero
    private Integer priority = 100;

    @Column(name = "last_matched_at")
    @ToString.Include
    private Instant lastMatchedAt;

}
