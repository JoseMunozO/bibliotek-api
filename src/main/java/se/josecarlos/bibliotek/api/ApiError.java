package se.josecarlos.bibliotek.api;

/**
 * JSON body returned to the client when a request fails.
 */
public record ApiError(int status, String message) {
}
