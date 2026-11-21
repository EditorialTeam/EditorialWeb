package com.editorial.auth;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Full context requires running Kafka and RSA key mount in container. Covered by AccountRepositoryTest.")
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
