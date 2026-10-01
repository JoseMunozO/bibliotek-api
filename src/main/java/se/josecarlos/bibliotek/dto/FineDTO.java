package se.josecarlos.bibliotek.dto;

import java.time.LocalDate;

public class FineDTO {

    private final int id;
    private final int loanId;
    private final String bookTitle;
    private final double amount;
    private final LocalDate issuedDate;
    private final LocalDate paidDate;
    private final String status;

    public FineDTO(int id, int loanId, String bookTitle, double amount, LocalDate issuedDate,
                   LocalDate paidDate, String status) {
        this.id = id;
        this.loanId = loanId;
        this.bookTitle = bookTitle;
        this.amount = amount;
        this.issuedDate = issuedDate;
        this.paidDate = paidDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getLoanId() {
        return loanId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public String getStatus() {
        return status;
    }
}
