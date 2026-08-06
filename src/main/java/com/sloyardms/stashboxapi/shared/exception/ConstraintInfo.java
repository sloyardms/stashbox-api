package com.sloyardms.stashboxapi.shared.exception;

import java.util.List;

public record ConstraintInfo(String detail, List<String> fields) {

    public static final ConstraintInfo UNKNOWN = new ConstraintInfo("constraint.unknown", List.of());

}
