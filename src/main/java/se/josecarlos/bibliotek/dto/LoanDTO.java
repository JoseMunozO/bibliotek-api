package se.josecarlos.bibliotek.dto;

import java.time.LocalDate;

public class LoanDTO {

    private int id;
    private int bookId;
    private String bookTitle;
    private int memberId;
    private String memberName;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    public LoanDTO(int id, int bookId, String bookTitle, int memberId, String memberName,
                   LocalDate loanDate, LocalDate dueDate, LocalDate returnDate) {
        this.id = id;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.memberName = memberName;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
    }

    public int getId() {
        return id;
    }

    public int getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }
}
