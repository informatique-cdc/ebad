package fr.icdc.ebad.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public final class ResponseUtil {

    private ResponseUtil() {
        // noop
    }

    public static <T> ResponseEntity<T> wrapOrNotFound(Optional<T> maybeResponse) {
        return wrapOrNotFound(maybeResponse, null);
    }

    public static <T> ResponseEntity<T> wrapOrNotFound(Optional<T> maybeResponse, HttpHeaders header) {
        return maybeResponse
                .map(response -> ResponseEntity.ok().headers(header).body(response))
                .orElse(ResponseEntity.notFound().build());
    }
}
