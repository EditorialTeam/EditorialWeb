package com.editorial.user.producer.event;

import java.util.UUID;

public record RegisteredProfile(UUID eventId, Long accountId) {
}
