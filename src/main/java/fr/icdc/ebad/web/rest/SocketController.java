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
public class SocketController {

//    @MessageMapping("/secured/chat")
//    @SendTo("/secured/history")
//    public OutputMessage send(Message msg) throws Exception {
//        return new OutputMessage(
//                msg.getFrom(),
//                msg.getText(),
//                new SimpleDateFormat("HH:mm").format(new Date()));
//    }

    private final SimpMessagingTemplate messagingTemplate;

    public SocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    public void processMessage(Principal principal, @Payload Message chatMessage) {
        try {
            System.err.println("DAMIEN");
            System.err.println(principal.getName());
            System.err.println(chatMessage);
            System.err.println(chatMessage.getTo());
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getTo(), "/queue/test",
                    new OutputMessage(
                            chatMessage.getFrom(),
                            chatMessage.getText(),
                            Date.from(Instant.now()).toString()));
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
