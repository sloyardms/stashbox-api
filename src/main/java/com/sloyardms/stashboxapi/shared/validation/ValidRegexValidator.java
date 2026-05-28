package com.sloyardms.stashboxapi.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ValidRegexValidator implements ConstraintValidator<ValidRegex, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.isBlank()){
            return true;
        }

        try {
            Pattern.compile(value);
            return true;
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }
}
