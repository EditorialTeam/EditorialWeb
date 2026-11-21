package com.editorial.auth.repository;

import com.editorial.auth.model.entity.Account;
import com.editorial.auth.model.enums.AccountRole;
import com.editorial.auth.model.enums.AccountStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class AccountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.6");

    @Autowired
    private AccountRepository repository;

    @Test
    void shouldSaveAndFindAccount() {
        Account account = new Account(
                "testuser",
                "test@example.com",
                "hashedpassword",
                AccountRole.USER,
                AccountStatus.ACTIVE
        );

        Account saved = repository.save(account);
        assertNotNull(saved.getId());

        Optional<Account> found = repository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals(AccountRole.USER, found.get().getRole());
        assertEquals(AccountStatus.ACTIVE, found.get().getStatus());

        assertTrue(repository.existsByUsername("testuser"));
        assertTrue(repository.existsByEmail("test@example.com"));
    }

    @Test
    void shouldCheckNonExistentAccount() {
        assertFalse(repository.existsByUsername("nonexistent"));
        assertFalse(repository.existsByEmail("nonexistent@example.com"));
        assertTrue(repository.findByEmail("nonexistent@example.com").isEmpty());
    }
}
