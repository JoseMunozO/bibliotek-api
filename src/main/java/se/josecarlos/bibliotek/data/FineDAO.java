package se.josecarlos.bibliotek.data;

import se.josecarlos.bibliotek.dto.FineDTO;
import se.josecarlos.bibliotek.model.Fine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PAID = "paid";

    private static final String FINE_DTO_SELECT = """
            SELECT f.id, f.loan_id, b.title AS book_title, f.amount, f.issued_date, f.paid_date, f.status
            FROM fines f
            JOIN loans l ON l.id = f.loan_id
            JOIN books b ON b.id = l.book_id
            """;

    public boolean createFine(Connection conn, int loanId, double amount) {
        String sql = """
                INSERT INTO fines (loan_id, amount, issued_date, status)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);
            stmt.setDouble(2, amount);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setString(4, STATUS_PENDING);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Could not create fine for loan " + loanId, e);
        }
    }

    public List<FineDTO> getFinesByMemberId(int memberId) {
        return queryFineDTOs(FINE_DTO_SELECT + " WHERE l.member_id = ? ORDER BY f.issued_date DESC, f.id DESC", memberId);
    }

    public FineDTO getFineDetailsById(int fineId) {
        List<FineDTO> fines = queryFineDTOs(FINE_DTO_SELECT + " WHERE f.id = ?", fineId);
        return fines.isEmpty() ? null : fines.getFirst();
    }

    public Fine getFineByIdForMember(int fineId, int memberId) {
        String sql = """
                SELECT f.*
                FROM fines f
                JOIN loans l ON f.loan_id = l.id
                WHERE f.id = ? AND l.member_id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fineId);
            stmt.setInt(2, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch fine " + fineId, e);
        }

        return null;
    }

    public boolean payFine(int fineId) {
        String sql = """
                UPDATE fines
                SET status = ?, paid_date = ?
                WHERE id = ? AND status <> ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, STATUS_PAID);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            stmt.setInt(3, fineId);
            stmt.setString(4, STATUS_PAID);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Could not pay fine " + fineId, e);
        }
    }

    public boolean hasFineForLoan(Connection conn, int loanId) {
        String sql = "SELECT 1 FROM fines WHERE loan_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not check fines for loan " + loanId, e);
        }
    }

    private List<FineDTO> queryFineDTOs(String sql, int param) {
        List<FineDTO> fines = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, param);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Date issuedDate = rs.getDate("issued_date");
                    Date paidDate = rs.getDate("paid_date");
                    fines.add(new FineDTO(
                            rs.getInt("id"),
                            rs.getInt("loan_id"),
                            rs.getString("book_title"),
                            rs.getDouble("amount"),
                            issuedDate != null ? issuedDate.toLocalDate() : null,
                            paidDate != null ? paidDate.toLocalDate() : null,
                            rs.getString("status")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch fines", e);
        }

        return fines;
    }

    private Fine mapRow(ResultSet rs) throws SQLException {
        Date issuedDate = rs.getDate("issued_date");

        return new Fine(
                rs.getInt("id"),
                rs.getInt("loan_id"),
                rs.getDouble("amount"),
                issuedDate != null ? issuedDate.toLocalDate() : null,
                rs.getString("status")
        );
    }
}
