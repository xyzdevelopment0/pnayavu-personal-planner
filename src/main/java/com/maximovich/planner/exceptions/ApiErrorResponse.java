package com.maximovich.planner.exceptions;

import java.time.LocalDateTime;

public record ApiErrorResponse(String message, LocalDateTime timestamp) {
}
