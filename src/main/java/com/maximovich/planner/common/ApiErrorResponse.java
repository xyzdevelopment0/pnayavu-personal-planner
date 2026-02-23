package com.maximovich.planner.common;

import java.time.LocalDateTime;

public record ApiErrorResponse(String message, LocalDateTime timestamp) {
}
