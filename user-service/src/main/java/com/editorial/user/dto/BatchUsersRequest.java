package com.editorial.user.dto;

import java.util.Set;

public record BatchUsersRequest(Set<Long> accountIds) {}
