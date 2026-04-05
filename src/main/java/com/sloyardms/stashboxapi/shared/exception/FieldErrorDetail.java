package com.sloyardms.stashboxapi.shared.exception;

import java.util.List;

public record FieldErrorDetail(String field, List<String> messages) {
}
