package com.editorial.user.repository;

import com.editorial.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByAccountId(Long accountId);
}
