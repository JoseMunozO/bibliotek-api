package se.josecarlos.bibliotek.api;

/**
 * JSON body for sending a notification. loanId is optional.
 */
public record NotificationRequest(int memberId, Integer loanId, String type, String message) {
}
