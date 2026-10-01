package se.josecarlos.bibliotek.data;

import se.josecarlos.bibliotek.dto.OverdueLoanDTO;
import se.josecarlos.bibliotek.model.Loan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    public void createLoan(int bookId, int memberId, LocalDate loanDate, LocalDate dueDate) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            createLoan(conn, bookId, memberId, loanDate, dueDate);
        } catch (SQLException e) {
            throw new DatabaseException("Could not create loan", e);
        }
    }

    public int createLoan(Connection conn, int bookId, int memberId, LocalDate loanDate, LocalDate dueDate) {
        String sql = """
                INSERT INTO loans (book_id, member_id, loan_date, due_date)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            stmt.setDate(3, Date.valueOf(loanDate));
            stmt.setDate(4, Date.valueOf(dueDate));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not create loan", e);
        }
    }

    public void returnLoan(int loanId, LocalDate returnDate) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            returnLoan(conn, loanId, returnDate);
        } catch (SQLException e) {
            throw new DatabaseException("Could not return loan " + loanId, e);
        }
    }

    public boolean returnLoan(Connection conn, int loanId, LocalDate returnDate) {
        String sql = """
                UPDATE loans
                SET return_date = ?
                WHERE id = ? AND return_date IS NULL
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(returnDate));
            stmt.setInt(2, loanId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Could not return loan " + loanId, e);
        }
    }

    public List<Loan> getActiveLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE return_date IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                loans.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch loans", e);
        }

        return loans;
    }

    public List<Loan> getLoansByMemberId(int memberId) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE member_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch loans", e);
        }

        return loans;
    }

    public Loan getActiveLoanById(int loanId) {
        String sql = "SELECT * FROM loans WHERE id = ? AND return_date IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch loans", e);
        }

        return null;
    }

    public Loan getLoanById(int loanId) {
        String sql = "SELECT * FROM loans WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch loans", e);
        }

        return null;
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        Date returnDateSql = rs.getDate("return_date");

        return new Loan(
                rs.getInt("id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                rs.getDate("loan_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                returnDateSql != null ? returnDateSql.toLocalDate() : null
        );
    }

    public Loan getActiveLoanByBookAndMember(int bookId, int memberId) {
        String sql = """
                SELECT * FROM loans
                WHERE book_id = ? AND member_id = ? AND return_date IS NULL
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch loans", e);
        }

        return null;
    }

    public boolean hasActiveLoanForBookAndMember(int bookId, int memberId) {
        return getActiveLoanByBookAndMember(bookId, memberId) != null;
    }

    public boolean hasReturnedLoanForBookAndMember(int bookId, int memberId) {
        String sql = """
                SELECT 1
                FROM loans
                WHERE book_id = ? AND member_id = ? AND return_date IS NOT NULL
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not check returned loans", e);
        }
    }

    public boolean extendLoan(int loanId, LocalDate newDueDate) {
        String sql = """
                UPDATE loans
                SET due_date = ?
                WHERE id = ? AND return_date IS NULL
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(newDueDate));
            stmt.setInt(2, loanId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not extend loan " + loanId, e);
        }
    }


    public List<Loan> getOverdueLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM loans WHERE return_date IS NULL AND due_date < CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                loans.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch loans", e);
        }

        return loans;
    }

    public List<OverdueLoanDTO> getOverdueLoanRegister() {
        List<OverdueLoanDTO> overdueLoans = new ArrayList<>();
        String sql = """
                SELECT
                    l.id AS loan_id,
                    b.id AS book_id,
                    b.title AS book_title,
                    m.id AS member_id,
                    CONCAT(m.first_name, ' ', m.last_name) AS member_name,
                    m.email AS member_email,
                    l.due_date
                FROM loans l
                JOIN books b ON b.id = l.book_id
                LEFT JOIN members m ON m.id = l.member_id
                WHERE l.return_date IS NULL AND l.due_date < CURDATE()
                ORDER BY l.due_date ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                overdueLoans.add(new OverdueLoanDTO(
                        rs.getInt("loan_id"),
                        rs.getInt("book_id"),
                        rs.getString("book_title"),
                        rs.getInt("member_id"),
                        rs.getString("member_name"),
                        rs.getString("member_email"),
                        rs.getDate("due_date").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch loans", e);
        }

        return overdueLoans;
    }
}
