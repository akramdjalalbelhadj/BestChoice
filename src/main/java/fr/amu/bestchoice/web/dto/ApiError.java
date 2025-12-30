package fr.amu.bestchoice.web.dto;

import java.time.Instant;
import java.util.Map;


public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, Object> details
) {
    /**
     * Méthode utilitaire pour créer rapidement une ApiError.
     */

    public static ApiError of(int status, String error, String message, String path, Map<String, Object> details) {
        return new ApiError(Instant.now(), status, error, message, path, details);
    }
}
