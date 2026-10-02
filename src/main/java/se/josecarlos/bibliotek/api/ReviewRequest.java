package se.josecarlos.bibliotek.api;

/**
 * JSON body for creating a review.
 */
public record ReviewRequest(int memberId, int rating, String comment) {
}
