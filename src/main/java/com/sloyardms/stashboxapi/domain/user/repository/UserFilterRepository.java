package com.sloyardms.stashboxapi.domain.user.repository;

import com.sloyardms.stashboxapi.domain.user.model.UserFilter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFilterRepository extends JpaRepository<UserFilter, UUID> {

}
