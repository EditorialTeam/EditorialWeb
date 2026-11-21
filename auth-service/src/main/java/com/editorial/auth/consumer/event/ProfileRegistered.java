package com.editorial.auth.consumer.event;

import java.util.UUID;

public record ProfileRegistered(UUID eventId, Long accountId) {}