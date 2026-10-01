package se.josecarlos.bibliotek.business;

/**
 * Thrown when input data is invalid.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
