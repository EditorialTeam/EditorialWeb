package com.editorial.user.repository;

import com.editorial.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.6");

    @Autowired
    private UserRepository repository;

    @Test
    void shouldSaveAndFindUser() {
        User user = new User(100L, "editorial_user", "Editorial User");
        user.setProfileDescription("Writer and editor");

        User saved = repository.save(user);
        assertNotNull(saved.getProfileId());

        Optional<User> found = repository.findByAccountId(100L);
        assertTrue(found.isPresent());
        assertEquals("editorial_user", found.get().getUsername());
        assertEquals("Editorial User", found.get().getDisplayName());
        assertEquals("Writer and editor", found.get().getProfileDescription());

        assertTrue(repository.existsByAccountId(100L));
        List<User> list = repository.findAllByAccountIdIn(List.of(100L, 999L));
        assertEquals(1, list.size());
    }

    @Test
    void shouldReturnEmptyForNonExistentAccountId() {
        assertFalse(repository.existsByAccountId(999L));
        assertTrue(repository.findByAccountId(999L).isEmpty());
    }
}
