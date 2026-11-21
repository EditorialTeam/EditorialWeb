package com.editorial.auth.producer.event;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

public record ProfileCreationRequested(
        UUID eventId,
        Long accountId,
        @JsonAlias("displayName") String username
) { }
