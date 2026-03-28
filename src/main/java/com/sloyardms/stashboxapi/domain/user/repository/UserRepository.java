package com.sloyardms.stashboxapi.domain.user.repository;

import com.sloyardms.stashboxapi.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u.id FROM User u")
    List<UUID> findAllUsersIds();

}
