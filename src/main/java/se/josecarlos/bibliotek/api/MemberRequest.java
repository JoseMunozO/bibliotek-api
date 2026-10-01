package se.josecarlos.bibliotek.api;

/**
 * JSON body for creating (membershipType ignored) or updating a member.
 */
public record MemberRequest(String firstName, String lastName, String email, String membershipType) {
}
