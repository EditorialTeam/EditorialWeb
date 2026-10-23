package com.editorial.user.dto;

import jakarta.validation.constraints.*;

public record RegisterUserRequest(@NotBlank @Size(max = 50) String username,
                                  @NotBlank @Email @Size(max=200) String email,
                                  @NotBlank @Size(max=100) String password) { }
