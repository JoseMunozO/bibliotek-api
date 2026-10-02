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
            throw new ValidationException("El ID del socio y el del libro deben ser mayores que 0.");
        }

        Member member = memberDAO.getMemberById(memberId);
        if (member == null) {
            throw new NotFoundException("Socio no encontrado.");
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            throw new NotFoundException("Libro no encontrado.");
        }

        if (!member.getStatus().equalsIgnoreCase("active")) {
            throw new ConflictException("El socio no está activo.");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new ConflictException("No quedan ejemplares disponibles.");
        }

        if (loanDAO.hasActiveLoanForBookAndMember(bookId, memberId)) {
            throw new ConflictException("Este socio ya tiene un préstamo activo de este libro.");
        }

        LocalDate loanDate = LocalDate.now();
        LocalDate dueDate = loanDate.plusDays(LOAN_PERIOD_DAYS);

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int loanId = loanDAO.createLoan(conn, bookId, memberId, loanDate, dueDate);

                // The UPDATE only succeeds while copies remain, which guards against two simultaneous loans
                if (!bookDAO.decreaseAvailableCopies(conn, bookId)) {
                    throw new ConflictException("No quedan ejemplares disponibles.");
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

        LoanDTO loan = loanDAO.getLoanDetailsById(loanId);
        if (loan == null) {
            throw new NotFoundException("Préstamo no encontrado.");
        }

        return loan;
    }

    public LoanReturnDTO returnBook(int loanId) {
        validateLoanId(loanId);

        Loan loan = loanDAO.getActiveLoanById(loanId);
        if (loan == null) {
            throw new NotFoundException("No se ha encontrado un préstamo activo.");
        }

        return processReturn(loan);
    }

    public LoanReturnDTO returnBookByMemberAndBook(int memberId, int bookId) {
        if (memberId <= 0 || bookId <= 0) {
            throw new ValidationException("El ID del socio y el del libro deben ser mayores que 0.");
        }

        Loan loan = loanDAO.getActiveLoanByBookAndMember(bookId, memberId);
        if (loan == null) {
            throw new NotFoundException("Este socio no tiene un préstamo activo de este libro.");
        }

        return processReturn(loan);
    }

    public List<LoanDTO> getActiveLoans() {
        return loanDAO.getActiveLoans();
    }

    public List<LoanDTO> getLoansByMemberId(int memberId) {
        if (memberId <= 0) {
            throw new ValidationException("ID de socio no válido.");
        }

        if (memberDAO.getMemberById(memberId) == null) {
            throw new NotFoundException("Socio no encontrado.");
        }

        return loanDAO.getLoansByMemberId(memberId);
    }

    public List<LoanDTO> getOverdueLoans() {
        return loanDAO.getOverdueLoans();
    }

    public List<OverdueLoanDTO> getOverdueLoanRegister() {
        return loanDAO.getOverdueLoanRegister();
    }

    public LoanDTO extendLoan(int loanId, int extraDays) {
        if (loanId <= 0 || extraDays <= 0) {
            throw new ValidationException("El ID del préstamo y los días extra deben ser mayores que 0.");
        }

        Loan loan = loanDAO.getActiveLoanById(loanId);
        if (loan == null) {
            throw new NotFoundException("No se ha encontrado un préstamo activo.");
        }

        if (loan.getDueDate().isBefore(LocalDate.now())) {
            throw new ConflictException("No se puede prorrogar un préstamo vencido.");
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
                    throw new ConflictException("El préstamo ya se ha devuelto.");
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
            throw new ValidationException("El ID del préstamo debe ser mayor que 0.");
        }
    }
}
