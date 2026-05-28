package com.sloyardms.stashboxapi.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*")
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidSlug {

    String message() default "Invalid slug format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
