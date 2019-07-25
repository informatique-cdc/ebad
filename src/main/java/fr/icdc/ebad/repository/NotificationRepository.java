package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the Notification entity.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverLoginAndReadFalse(Sort sort, String login);
}
