package com.sloyardms.stashboxapi.shared.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for manipulating {@link Pageable} instances.
 */
public class PageableUtils {

    private PageableUtils() {
    }

    /**
     * Remaps sort field names in a {@link Pageable} using the provided field mappings.
     * <p>
     * Useful when API-facing field names differ from their underlying column names in native queries.
     * Fields not present in the mapping are left unchanged.
     *
     * @param pageable      the original pageable instance
     * @param fieldMappings a map of API field names with their corresponding column names
     * @return a new {@link Pageable} with remapped sort fields
     */
    public static Pageable remapSort(Pageable pageable, Map<String, String> fieldMappings) {
        List<Sort.Order> mappedOrders = new ArrayList<>(pageable.getSort().stream()
                .map(order -> {
                    String mapped = fieldMappings.getOrDefault(order.getProperty(), order.getProperty());
                    return order.withProperty(mapped);
                })
                .toList());

        boolean hasIdTiebreaker = mappedOrders.stream().anyMatch(o -> o.getProperty().equals("id"));
        if (!hasIdTiebreaker) {
            mappedOrders.add(Sort.Order.asc("id"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(mappedOrders));
    }

}
