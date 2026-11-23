package com.editorial.auth.dto.auth;

import jakarta.validation.constraints.*;

public record RegisterAccountRequest(@NotBlank @Size(max = 20) String username,
                                  @NotBlank @Email @Size(max=254) String email,
                                  @NotBlank @Size(min=6, max=100) String password,
                                     @Size(min=0, max=40) String displayName) { }
