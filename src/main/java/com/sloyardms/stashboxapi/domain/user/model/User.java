package com.sloyardms.stashboxapi.domain.user.model;

import com.sloyardms.stashboxapi.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "users")
public class User extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @ToString.Include
    private UUID id;

    @Column(name = "settings", columnDefinition = "jsonb not null default '{}'::jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private UserSettings settings = new UserSettings();

}
