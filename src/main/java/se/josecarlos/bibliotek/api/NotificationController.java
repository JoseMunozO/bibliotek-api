package se.josecarlos.bibliotek.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import se.josecarlos.bibliotek.business.NotificationService;
import se.josecarlos.bibliotek.dto.NotificationDTO;

import java.util.List;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/api/members/{memberId}/notifications")
    public List<NotificationDTO> getNotifications(@PathVariable int memberId) {
        return notificationService.getNotificationsByMemberId(memberId);
    }

    @PostMapping("/api/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationDTO sendNotification(@RequestBody NotificationRequest request) {
        return notificationService.sendNotification(request.memberId(), request.loanId(),
                request.type(), request.message());
    }

    @PostMapping("/api/notifications/{id}/read")
    public NotificationDTO markAsRead(@PathVariable int id) {
        return notificationService.markAsRead(id);
    }
}
