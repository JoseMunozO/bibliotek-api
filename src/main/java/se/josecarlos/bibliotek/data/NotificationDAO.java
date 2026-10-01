package se.josecarlos.bibliotek.data;

import se.josecarlos.bibliotek.dto.NotificationDTO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private static final String NOTIFICATION_SELECT = """
            SELECT id, member_id, loan_id, type, message, sent_date, is_read
            FROM notifications
            """;

    public int createNotification(int memberId, Integer loanId, String type, String message) {
        String sql = """
                INSERT INTO notifications (member_id, loan_id, type, message, sent_date, is_read)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, memberId);
            if (loanId == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, loanId);
            }
            stmt.setString(3, type);
            stmt.setString(4, message);
            stmt.setDate(5, Date.valueOf(LocalDate.now()));
            stmt.setBoolean(6, false);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not create notification", e);
        }
    }

    public List<NotificationDTO> getNotificationsByMemberId(int memberId) {
        return queryNotifications(NOTIFICATION_SELECT + " WHERE member_id = ? ORDER BY sent_date DESC, id DESC", memberId);
    }

    public NotificationDTO getNotificationById(int notificationId) {
        List<NotificationDTO> notifications = queryNotifications(NOTIFICATION_SELECT + " WHERE id = ?", notificationId);
        return notifications.isEmpty() ? null : notifications.getFirst();
    }

    public void markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Could not mark notification " + notificationId + " as read", e);
        }
    }

    private List<NotificationDTO> queryNotifications(String sql, int param) {
        List<NotificationDTO> notifications = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, param);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch notifications", e);
        }

        return notifications;
    }

    private NotificationDTO mapRow(ResultSet rs) throws SQLException {
        Date sentDate = rs.getDate("sent_date");
        Integer loanId = (Integer) rs.getObject("loan_id");

        return new NotificationDTO(
                rs.getInt("id"),
                rs.getInt("member_id"),
                loanId,
                rs.getString("type"),
                rs.getString("message"),
                sentDate != null ? sentDate.toLocalDate() : null,
                rs.getBoolean("is_read")
        );
    }
}
