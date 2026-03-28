package com.sloyardms.stashboxapi.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates if at least one field of the entity is non-null
 * Used for partial update requests
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneNonNullFieldValidator.class)
public @interface AtLeastOneNonNullField {

    String message() default "At least one field must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
