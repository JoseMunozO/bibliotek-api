package se.josecarlos.bibliotek.api;

/**
 * JSON body for borrowing a book.
 */
public record LoanRequest(int memberId, int bookId) {
}
