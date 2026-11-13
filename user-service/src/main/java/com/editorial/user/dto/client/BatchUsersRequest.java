package com.editorial.user.dto.client;

import java.util.Set;

public record BatchUsersRequest(Set<Long> accountIds) {}
