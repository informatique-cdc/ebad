package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.websocket.Message;
import fr.icdc.ebad.config.websocket.OutputMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.sql.Date;
import java.time.Instant;

@Controller
//FIXME DTROUILLET REMOVE THIS TEST
public class SocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public SocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void processMessage(Principal principal, @Payload Message chatMessage) {
        messagingTemplate.convertAndSendToUser(
                chatMessage.getTo(), "/queue/test",
                new OutputMessage(
                        chatMessage.getFrom(),
                        chatMessage.getText(),
                        Date.from(Instant.now()).toString()));
    }
}
