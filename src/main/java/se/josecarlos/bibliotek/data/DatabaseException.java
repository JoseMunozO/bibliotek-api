package se.josecarlos.bibliotek.data;

/**
 * Wraps SQL errors so callers do not need to handle checked SQLExceptions.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
