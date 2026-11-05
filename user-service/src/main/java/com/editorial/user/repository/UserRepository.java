package com.editorial.user.repository;

import com.editorial.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByAccountId(Long accountId);
    Optional<User> findByAccountId(Long accountId);
}
