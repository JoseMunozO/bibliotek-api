package se.josecarlos.bibliotek.api;

/**
 * JSON body for extending a loan.
 */
public record ExtendLoanRequest(int extraDays) {
}
