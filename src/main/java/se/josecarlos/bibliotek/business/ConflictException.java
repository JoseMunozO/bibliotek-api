package se.josecarlos.bibliotek.business;

/**
 * Thrown when an operation conflicts with existing data, e.g. a duplicate email.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
