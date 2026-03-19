package com.sloyardms.stashboxapi.domain.user.model;

import com.sloyardms.stashboxapi.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "users_provider_id_unique", columnNames = "provider_id"),
                @UniqueConstraint(name = "users_username_unique", columnNames = "username"),
                @UniqueConstraint(name = "users_email_unique", columnNames = "email")
        })
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(name = "email", nullable = false)
    @ToString.Include
    private String email;

    @Column(name = "username", nullable = false)
    @ToString.Include
    private String username;

    @Column(name = "settings", columnDefinition = "jsonb not null default '{}'::jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private UserSettings settings = new UserSettings();

}
