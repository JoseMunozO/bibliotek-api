package se.josecarlos.bibliotek.data;

import se.josecarlos.bibliotek.model.Fine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    public void createFine(int loanId, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            createFine(conn, loanId, amount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createFine(Connection conn, int loanId, double amount) {
        String sql = """
                INSERT INTO fines (loan_id, amount, issued_date, status)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loanId);
            stmt.setDouble(2, amount);
            stmt.setDate(3, Date.valueOf(java.time.LocalDate.now()));
            stmt.setString(4, "UNPAID");

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DatabaseException("Could not create fine for loan " + loanId, e);
        }
    }

    public List<Fine> getFinesByMemberId(int memberId) {
        List<Fine> fines = new ArrayList<>();

        String sql = """
                SELECT f.*
                FROM fines f
                JOIN loans l ON f.loan_id = l.id
                WHERE l.member_id = ?
                ORDER BY f.issued_date DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fines.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fines;
    }

    public Fine getFineById(int fineId) {
        String sql = "SELECT * FROM fines WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fineId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
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
            e.printStackTrace();
        }

        return null;
    }

    public boolean payFine(int fineId) {
        String sql = """
                UPDATE fines
                SET status = 'PAID'
                WHERE id = ? AND status <> 'PAID'
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fineId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasFineForLoan(int loanId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return hasFineForLoan(conn, loanId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
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
