package com.editorial.auth.producer.event;

import java.util.UUID;

public record ProfileCreationRequested(
        UUID eventId,
        Long accountId,
        String displayName
) { }