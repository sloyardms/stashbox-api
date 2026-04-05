package com.sloyardms.stashboxapi.infrastructure.web;

import com.sloyardms.stashboxapi.shared.validation.SortableFields;
import com.sloyardms.stashboxapi.shared.exception.types.InvalidSortFieldException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class PageSortArgumentResolver implements HandlerMethodArgumentResolver {

    private final PageableHandlerMethodArgumentResolver pageableHandlerMethodArgumentResolver;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(SortableFields.class) && Pageable.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public @Nullable Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
                                            @NonNull NativeWebRequest webRequest,
                                            @Nullable WebDataBinderFactory binderFactory) throws Exception {
        Pageable pageable = pageableHandlerMethodArgumentResolver.resolveArgument(
                parameter, mavContainer, webRequest, binderFactory);

        SortableFields annotation = parameter.getParameterAnnotation(SortableFields.class);
        Set<String> allowed = Set.of(annotation.value());

        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw new InvalidSortFieldException(order.getProperty(), allowed);
            }
        }

        if (!pageable.getSort().isSorted()
                && !annotation.defaultField().isBlank()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(annotation.defaultDirection(), annotation.defaultField())
            );
        }

        return pageable;
    }

}
