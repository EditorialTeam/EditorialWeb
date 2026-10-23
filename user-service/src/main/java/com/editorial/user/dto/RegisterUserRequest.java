package com.editorial.user.dto;

import jakarta.validation.constraints.*;

public record RegisterUserRequest(@NotBlank @Size(max = 100) String username,
                                  @NotBlank @Email @Size(max=254) String email,
                                  @NotBlank @Size(min=6, max=100) String password) { }
