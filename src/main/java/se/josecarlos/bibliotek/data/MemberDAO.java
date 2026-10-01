package se.josecarlos.bibliotek.data;

import se.josecarlos.bibliotek.dto.MemberProfileDTO;
import se.josecarlos.bibliotek.model.Member;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                members.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch members", e);
        }

        return members;
    }

    public Member getMemberById(int id) {
        String sql = "SELECT * FROM members WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch member " + id, e);
        }

        return null;
    }

    public int createMember(Member member) {
        String sql = """
                INSERT INTO members (first_name, last_name, email, membership_date, membership_type, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setString(3, member.getEmail());
            stmt.setDate(4, Date.valueOf(member.getMembershipDate()));
            stmt.setString(5, member.getMembershipType());
            stmt.setString(6, member.getStatus());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not create member", e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM members WHERE LOWER(email) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not check member email", e);
        }
    }

    public boolean existsByEmailExcludingMember(String email, int memberId) {
        String sql = "SELECT 1 FROM members WHERE LOWER(email) = LOWER(?) AND id <> ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not check member email", e);
        }
    }

    public void updateMember(int memberId, String firstName, String lastName, String email, String membershipType) {
        String sql = """
                UPDATE members
                SET first_name = ?, last_name = ?, email = ?, membership_type = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, membershipType);
            stmt.setInt(5, memberId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update member " + memberId, e);
        }
    }

    public void updateStatus(int memberId, String status) {
        String sql = "UPDATE members SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, memberId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update status for member " + memberId, e);
        }
    }

    public MemberProfileDTO getMemberProfile(int memberId) {
        String sql = """
                SELECT
                    m.id,
                    m.first_name,
                    m.last_name,
                    m.email,
                    m.membership_date,
                    m.membership_type,
                    m.status,
                    COALESCE(SUM(CASE WHEN l.return_date IS NULL THEN 1 ELSE 0 END), 0) AS active_loans_count,
                    COUNT(l.id) AS total_loans_count,
                    COUNT(DISTINCT f.id) AS total_fines_count,
                    COALESCE(SUM(CASE WHEN f.status <> 'PAID' THEN f.amount ELSE 0 END), 0) AS unpaid_fine_amount
                FROM members m
                LEFT JOIN loans l ON l.member_id = m.id
                LEFT JOIN fines f ON f.loan_id = l.id
                WHERE m.id = ?
                GROUP BY m.id, m.first_name, m.last_name, m.email, m.membership_date, m.membership_type, m.status
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MemberProfileDTO(
                            rs.getInt("id"),
                            rs.getString("first_name") + " " + rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getDate("membership_date").toLocalDate(),
                            rs.getString("membership_type"),
                            rs.getString("status"),
                            rs.getInt("active_loans_count"),
                            rs.getInt("total_loans_count"),
                            rs.getInt("total_fines_count"),
                            rs.getDouble("unpaid_fine_amount")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch profile for member " + memberId, e);
        }

        return null;
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getDate("membership_date").toLocalDate(),
                rs.getString("membership_type"),
                rs.getString("status")
        );
    }
}
