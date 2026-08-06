package com.sloyardms.stashboxapi.shared.exception.types;

import com.sloyardms.stashboxapi.shared.exception.FieldErrorDetail;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class FieldValidationException extends RuntimeException {

    private final List<FieldErrorDetail> fieldErrors;

    public FieldValidationException(String field, String message) {
        this.fieldErrors = List.of(new FieldErrorDetail(field, message));
    }

    public FieldValidationException(List<FieldErrorDetail> fieldErrors) {
        this.fieldErrors = Collections.unmodifiableList(fieldErrors);
    }

}
