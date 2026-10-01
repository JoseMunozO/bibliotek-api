package se.josecarlos.bibliotek.business;

import se.josecarlos.bibliotek.data.BookDAO;
import se.josecarlos.bibliotek.data.DatabaseConnection;
import se.josecarlos.bibliotek.data.DatabaseException;
import se.josecarlos.bibliotek.data.FineDAO;
import se.josecarlos.bibliotek.data.LoanDAO;
import se.josecarlos.bibliotek.data.MemberDAO;
import se.josecarlos.bibliotek.dto.LoanDTO;
import se.josecarlos.bibliotek.dto.LoanReturnDTO;
import se.josecarlos.bibliotek.dto.OverdueLoanDTO;
import se.josecarlos.bibliotek.mapper.LoanMapper;
import se.josecarlos.bibliotek.model.Book;
import se.josecarlos.bibliotek.model.Loan;
import se.josecarlos.bibliotek.model.Member;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LoanService {

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double FINE_PER_LATE_DAY = 2.0;

    private final LoanDAO loanDAO;
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final FineDAO fineDAO;

    public LoanService() {
        this.loanDAO = new LoanDAO();
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        this.fineDAO = new FineDAO();
    }

    public LoanDTO borrowBook(int memberId, int bookId) {
        if (memberId <= 0 || bookId <= 0) {
            throw new ValidationException("Member ID and book ID must be greater than 0.");
        }

        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            throw new NotFoundException("Member not found.");
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            throw new NotFoundException("Book not found.");
        }

        if (!member.getStatus().equalsIgnoreCase("ACTIVE")) {
            throw new ConflictException("Member is not active.");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new ConflictException("No available copies.");
        }

        if (loanDAO.hasActiveLoanForBookAndMember(bookId, memberId)) {
            throw new ConflictException("This member already has an active loan for this book.");
        }

        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = loanDate.plusDays(LOAN_PERIOD_DAYS);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int loanId = loanDAO.createLoan(conn, bookId, memberId, loanDate, dueDate);

                // The UPDATE only succeeds while copies remain, which guards against two simultaneous loans
                if (!bookDAO.decreaseAvailableCopies(conn, bookId)) {
                    throw new ConflictException("No available copies.");
                }

                conn.commit();
                return getLoan(loanId);
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not create the loan", e);
        }
    }

    public LoanDTO getLoan(int loanId) {
        validateLoanId(loanId);

        Loan loan = loanDAO.getLoanById(loanId);
        if (loan == null) {
            throw new NotFoundException("Loan not found.");
        }

        return LoanMapper.toDTO(loan);
    }

    public LoanReturnDTO returnBook(int loanId) {
        validateLoanId(loanId);

        Loan loan = loanDAO.getActiveLoanById(loanId);
        if (loan == null) {
            throw new NotFoundException("Active loan not found.");
        }

        return processReturn(loan);
    }

    public LoanReturnDTO returnBookByMemberAndBook(int memberId, int bookId) {
        if (memberId <= 0 || bookId <= 0) {
            throw new ValidationException("Member ID and book ID must be greater than 0.");
        }

        Loan loan = loanDAO.getActiveLoanByBookAndMember(bookId, memberId);
        if (loan == null) {
            throw new NotFoundException("Active loan not found for this member and book.");
        }

        return processReturn(loan);
    }

    public List<LoanDTO> getActiveLoans() {
        return loanDAO.getActiveLoans().stream()
                .map(LoanMapper::toDTO)
                .toList();
    }

    public List<LoanDTO> getLoansByMemberId(int memberId) {
        if (memberId <= 0) {
            throw new ValidationException("Invalid member ID.");
        }

        if (memberDAO.getMemberById(memberId) == null) {
            throw new NotFoundException("Member not found.");
        }

        return loanDAO.getLoansByMemberId(memberId).stream()
                .map(LoanMapper::toDTO)
                .toList();
    }

    public List<LoanDTO> getOverdueLoans() {
        return loanDAO.getOverdueLoans().stream()
                .map(LoanMapper::toDTO)
                .toList();
    }

    public List<OverdueLoanDTO> getOverdueLoanRegister() {
        return loanDAO.getOverdueLoanRegister();
    }

    public LoanDTO extendLoan(int loanId, int extraDays) {
        if (loanId <= 0 || extraDays <= 0) {
            throw new ValidationException("Loan ID and extra days must be greater than 0.");
        }

        Loan loan = loanDAO.getActiveLoanById(loanId);
        if (loan == null) {
            throw new NotFoundException("Active loan not found.");
        }

        if (loan.getDueDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Cannot extend an overdue loan.");
        }

        LocalDate newDueDate = loan.getDueDate().plusDays(extraDays);
        loanDAO.extendLoan(loanId, newDueDate);
        return getLoan(loanId);
    }

    private LoanReturnDTO processReturn(Loan loan) {
        LocalDate today = LocalDate.now();
        double fineAmount = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!loanDAO.returnLoan(conn, loan.getId(), today)) {
                    throw new ConflictException("Loan has already been returned.");
                }

                bookDAO.increaseAvailableCopies(conn, loan.getBookId());

                if (today.isAfter(loan.getDueDate()) && !fineDAO.hasFineForLoan(conn, loan.getId())) {
                    long lateDays = ChronoUnit.DAYS.between(loan.getDueDate(), today);
                    fineAmount = lateDays * FINE_PER_LATE_DAY;
                    fineDAO.createFine(conn, loan.getId(), fineAmount);
                }

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not complete the return", e);
        }

        return new LoanReturnDTO(getLoan(loan.getId()), fineAmount);
    }

    private void validateLoanId(int loanId) {
        if (loanId <= 0) {
            throw new ValidationException("Loan ID must be greater than 0.");
        }
    }
}
