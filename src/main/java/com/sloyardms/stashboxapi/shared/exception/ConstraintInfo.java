package com.sloyardms.stashboxapi.shared.exception;

public record ConstraintInfo(String detail, String field) {

    public static final ConstraintInfo UNKNOWN = new ConstraintInfo("constraint.unknown", null);

}
