package com.sloyardms.stashboxapi.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidRegexValidator.class)
public @interface ValidRegex {

    String message() default "The provided value is not a valid regular expression";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
