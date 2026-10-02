package se.josecarlos.bibliotek.data;

import se.josecarlos.bibliotek.dto.ReviewDTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    private static final String REVIEW_SELECT = """
            SELECT
                r.id,
                r.book_id,
                r.member_id,
                CONCAT(COALESCE(m.first_name, ''), ' ', COALESCE(m.last_name, '')) AS member_name,
                r.rating,
                r.comment,
                r.review_date
            FROM reviews r
            LEFT JOIN members m ON m.id = r.member_id
            """;

    public List<ReviewDTO> getReviewsByBookId(int bookId) {
        return queryReviews(REVIEW_SELECT + " WHERE r.book_id = ? ORDER BY r.review_date DESC, r.id DESC", bookId);
    }

    public ReviewDTO getReviewById(int reviewId) {
        List<ReviewDTO> reviews = queryReviews(REVIEW_SELECT + " WHERE r.id = ?", reviewId);
        return reviews.isEmpty() ? null : reviews.getFirst();
    }

    public boolean hasMemberReviewedBook(int memberId, int bookId) {
        String sql = "SELECT 1 FROM reviews WHERE member_id = ? AND book_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, memberId);
            stmt.setInt(2, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not check reviews", e);
        }
    }

    public int createReview(int bookId, int memberId, int rating, String comment) {
        String sql = """
                INSERT INTO reviews (book_id, member_id, rating, comment, review_date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, bookId);
            stmt.setInt(2, memberId);
            stmt.setInt(3, rating);
            stmt.setString(4, comment);
            stmt.setDate(5, Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not create review", e);
        }
    }

    private List<ReviewDTO> queryReviews(String sql, int param) {
        List<ReviewDTO> reviews = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, param);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch reviews", e);
        }

        return reviews;
    }

    private ReviewDTO mapRow(ResultSet rs) throws SQLException {
        Date reviewDate = rs.getDate("review_date");
        String memberName = rs.getString("member_name");
        String normalizedMemberName = memberName == null ? "" : memberName.trim();

        return new ReviewDTO(
                rs.getInt("id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                normalizedMemberName.isEmpty() ? "Unknown member" : normalizedMemberName,
                rs.getInt("rating"),
                rs.getString("comment"),
                reviewDate != null ? reviewDate.toLocalDate() : null
        );
    }
}
