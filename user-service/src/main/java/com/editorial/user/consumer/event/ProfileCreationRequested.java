package com.editorial.user.consumer.event;

import java.util.UUID;

public record ProfileCreationRequested(UUID eventId, Long accountId, String displayName) {
}
