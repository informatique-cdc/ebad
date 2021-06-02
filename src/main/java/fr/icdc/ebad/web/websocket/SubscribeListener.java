package fr.icdc.ebad.web.websocket;

import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class SubscribeListener implements ApplicationListener<SessionSubscribeEvent> {

    private final SimpMessagingTemplate messagingTemplate;

    public SubscribeListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        String destination = (String)event.getMessage().getHeaders().get("simpDestination");
        if(destination.startsWith("/topic/env")) {
            System.out.println(event);
            System.out.println(event.getUser().getName());
            System.out.println(event.getUser());
            messagingTemplate.convertAndSendToUser(event.getUser().getName(), "/topic/env/2930", "Last known error count");
        }
    }
}