package com.editorial.user.repository;

import com.editorial.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByAccountId(Long accountId);
    Optional<User> findByAccountId(Long accountId);
    List<User> findAllByAccountIdIn(Collection<Long> accountIds);
}
