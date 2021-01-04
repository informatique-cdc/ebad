package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Notification;
import fr.icdc.ebad.service.NotificationService;
import fr.icdc.ebad.web.rest.dto.NotificationDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;

/**
 * Created by dtrouillet on 21/03/2018.
 */
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "the notification API")
public class NotificationResource {
    private final NotificationService notificationService;
    private final MapperFacade mapper;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationResource(NotificationService notificationService, MapperFacade mapper, SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.mapper = mapper;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpDestination = (String) message.getHeaders().get("simpDestination");

        if (simpDestination.startsWith("/user/queue/notifications")) {
            String username = event.getUser().getName();
            List<Notification> notificationList = notificationService.getNewNotificationsForUser(username);
            if(notificationList.isEmpty()){
                return;
            }

            List<NotificationDto> notificationDtos = mapper.mapAsList(notificationList, NotificationDto.class);
            this.messagingTemplate.convertAndSendToUser(username,"/queue/notifications",notificationDtos);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getUnreadNotification() {
        List<Notification> notificationList = notificationService.getNewNotificationsForCurrentUser();
        return ResponseEntity.ok(mapper.mapAsList(notificationList, NotificationDto.class));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping
    public ResponseEntity<Void> markAsReadNotification() {
        notificationService.markNotificationAsReadForCurrentUser();
        return ResponseEntity.ok().build();
    }
}
