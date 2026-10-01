package se.josecarlos.bibliotek.business;

import se.josecarlos.bibliotek.data.LoanDAO;
import se.josecarlos.bibliotek.data.MemberDAO;
import se.josecarlos.bibliotek.data.NotificationDAO;
import se.josecarlos.bibliotek.dto.NotificationDTO;
import se.josecarlos.bibliotek.model.Loan;

import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;
    private final MemberDAO memberDAO;
    private final LoanDAO loanDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
        this.memberDAO = new MemberDAO();
        this.loanDAO = new LoanDAO();
    }

    public NotificationDTO sendNotification(int memberId, Integer loanId, String type, String message) {
        // Types are stored in snake_case like the seed data, e.g. "Pending fine" -> "pending_fine"
        String normalizedType = type == null ? "" : type.trim().toLowerCase().replaceAll("\\s+", "_");
        String normalizedMessage = message == null ? "" : message.trim();

        validateMemberExists(memberId);

        if (normalizedType.isEmpty() || normalizedMessage.isEmpty()) {
            throw new ValidationException("Type and message are required.");
        }

        if (loanId != null) {
            Loan loan = loanDAO.getLoanById(loanId);
            if (loan == null) {
                throw new NotFoundException("Loan not found.");
            }

            if (loan.getMemberId() != memberId) {
                throw new ValidationException("That loan does not belong to the selected member.");
            }
        }

        int notificationId = notificationDAO.createNotification(memberId, loanId, normalizedType, normalizedMessage);
        return notificationDAO.getNotificationById(notificationId);
    }

    public List<NotificationDTO> getNotificationsByMemberId(int memberId) {
        validateMemberExists(memberId);
        return notificationDAO.getNotificationsByMemberId(memberId);
    }

    public NotificationDTO markAsRead(int notificationId) {
        if (notificationId <= 0) {
            throw new ValidationException("Invalid notification ID.");
        }

        if (notificationDAO.getNotificationById(notificationId) == null) {
            throw new NotFoundException("Notification not found.");
        }

        notificationDAO.markAsRead(notificationId);
        return notificationDAO.getNotificationById(notificationId);
    }

    private void validateMemberExists(int memberId) {
        if (memberId <= 0) {
            throw new ValidationException("Member ID must be greater than 0.");
        }

        if (memberDAO.getMemberById(memberId) == null) {
            throw new NotFoundException("Member not found.");
        }
    }
}
