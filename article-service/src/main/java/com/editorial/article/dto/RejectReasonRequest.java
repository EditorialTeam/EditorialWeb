package com.editorial.article.dto;

import jakarta.validation.constraints.Size;

public record RejectReasonRequest(@Size(min=1) String rejectionReason) {
}
