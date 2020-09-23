package fr.icdc.ebad.web.rest.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Utility class for handling pagination.
 *
 */
public class PaginationUtil {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;

    private PaginationUtil() {
    }

    public static Pageable generatePageRequestOrDefault(Pageable pageable) {
        if (pageable != null) {
            return pageable;
        }
        return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
    }
}
