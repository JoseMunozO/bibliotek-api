package se.josecarlos.bibliotek.dto;

public class LoanReturnDTO {

    private final LoanDTO loan;
    private final double fineAmount;

    public LoanReturnDTO(LoanDTO loan, double fineAmount) {
        this.loan = loan;
        this.fineAmount = fineAmount;
    }

    public LoanDTO getLoan() {
        return loan;
    }

    public double getFineAmount() {
        return fineAmount;
    }
}
