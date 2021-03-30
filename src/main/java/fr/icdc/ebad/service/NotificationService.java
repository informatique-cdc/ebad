package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Notification;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.NotificationRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.SecurityUtils;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by dtrouillet on 21/03/2018.
 */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void createNotification(String message, User user, boolean isDanger) {
        if (null == user) {
            throw new IllegalStateException("User not found");
        }
        Notification notification = new Notification();
        notification.setContent(message);
        notification.setCreatedDate(DateTime.now());
        notification.setReceiver(user);
        notification.setDanger(isDanger);
        Notification result = notificationRepository.save(notification);
        this.messagingTemplate.convertAndSendToUser(user.getLogin(), "/queue/notifications", result);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNewNotificationsForCurrentUser() {
        return getNewNotificationsForUser(SecurityUtils.getCurrentLogin());
    }

    @Transactional(readOnly = true)
    public List<Notification> getNewNotificationsForUser(String username) {
        return notificationRepository.findByReceiverLoginAndReadFalse(Sort.by("createdDate"), username);
    }

    @Transactional
    public void markNotificationAsReadForCurrentUser() {
        List<Notification> notifications = notificationRepository.findByReceiverLoginAndReadFalse(Sort.by("createdDate"), SecurityUtils.getCurrentLogin());
        notifications.forEach(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}
