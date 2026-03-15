package com.sloyardms.stashboxapi.user.repository;

import com.sloyardms.stashboxapi.user.model.UserFilter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFilterRepository extends JpaRepository<UserFilter, UUID> {

}
