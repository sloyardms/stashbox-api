package com.sloyardms.stashboxapi.shared.validation;

import org.springframework.data.domain.Sort;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SortableFields {

    String[] value();

    String defaultField() default "";

    Sort.Direction defaultDirection() default Sort.Direction.ASC;

}
