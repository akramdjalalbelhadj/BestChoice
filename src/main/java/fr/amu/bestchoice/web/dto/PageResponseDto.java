package fr.amu.bestchoice.web.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * 📄 Wrapper pour les réponses paginées.
 */
public record PageResponseDto<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponseDto<T> of(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
