package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Notification;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.NotificationRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.SecurityUtils;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Created by dtrouillet on 21/03/2018.
 */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createNotification(String message) {
        Optional<User> user = userRepository.findOneByLoginUser(SecurityUtils.getCurrentLogin());
        if (!user.isPresent()) {
            throw new IllegalStateException("User not found");
        }
        Notification notification = new Notification();
        notification.setContent(message);
        notification.setCreatedDate(DateTime.now());
        notification.setReceiver(user.get());

        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNewNotificationsForCurrentUser() {
        return notificationRepository.findByReceiverLoginAndReadFalse(Sort.by("createdDate"), SecurityUtils.getCurrentLogin());
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
