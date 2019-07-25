package fr.icdc.ebad.web.rest.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for handling pagination.
 *
 * <p>
 * Pagination uses the same principles as the <a href="https://developer.github.com/v3/#pagination">Github API</api>,
 * and follow <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 (Link header)</a>.
 * </p>
 */
public class PaginationUtil {

    public static final int DEFAULT_OFFSET = 1;

    public static final int MIN_OFFSET = 1;

    public static final int DEFAULT_LIMIT = 10000;

    public static final int MAX_LIMIT = 10000;

    public static final String PAGE = "?page=";

    public static final String PER_PAGE = "&per_page=";

    private PaginationUtil() {
    }

    public static Pageable generatePageRequest(Integer pOffset, Integer pLimit) {
        Integer offset = pOffset;
        Integer limit = pLimit;
        if (pOffset == null || pOffset < MIN_OFFSET) {
            offset = DEFAULT_OFFSET;
        }
        if (pLimit == null || pLimit > MAX_LIMIT) {
            limit = DEFAULT_LIMIT;
        }
        return PageRequest.of(offset - 1, limit);
    }

    public static <T> HttpHeaders generatePaginationHttpHeaders(Page<T> page, String baseUrl, Integer pOffset, Integer pLimit)
        throws URISyntaxException {
        Integer offset = pOffset;
        Integer limit = pLimit;
        if (pOffset == null || pOffset < MIN_OFFSET) {
            offset = DEFAULT_OFFSET;
        }
        if (pLimit == null || pLimit > MAX_LIMIT) {
            limit = DEFAULT_LIMIT;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", Long.toString(page.getTotalElements()));
        String link = "";
        if (offset < page.getTotalPages()) {
            link = "<" + (new URI(baseUrl + PAGE + (offset + 1) + PER_PAGE + limit)).toString()
                + ">; rel=\"next\",";
        }
        if (offset > 1) {
            link += "<" + (new URI(baseUrl + PAGE + (offset - 1) + PER_PAGE + limit)).toString()
                + ">; rel=\"prev\",";
        }
        link += "<" + (new URI(baseUrl + PAGE + page.getTotalPages() + PER_PAGE + limit)).toString()
            + ">; rel=\"last\"," +
            "<" + (new URI(baseUrl + PAGE + 1 + PER_PAGE + limit)).toString()
            + ">; rel=\"first\"";
        headers.add(HttpHeaders.LINK, link);
        return headers;
    }
}
