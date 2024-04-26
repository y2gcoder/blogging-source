package com.y2gcoder.learningcode.pageablerequest.presentation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import org.springframework.data.domain.Page;

@JsonNaming(SnakeCaseStrategy.class)
public record PageResponse<T>(
        int totalPages,
        long totalElements,
        int number,
        int size,
        int numberOfElements,
        List<T> content
) {

    public PageResponse(Page<T> page) {
        this(page.getTotalPages(), page.getTotalElements(), page.getNumber(), page.getSize(), page.getNumberOfElements(), page.getContent());
    }
}
