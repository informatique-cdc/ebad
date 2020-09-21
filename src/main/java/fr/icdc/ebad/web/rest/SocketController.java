package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.websocket.Message;
import fr.icdc.ebad.config.websocket.OutputMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class SocketController {

    @MessageMapping("/secured/chat")
    @SendTo("/secured/history")
    public OutputMessage send(Message msg) throws Exception {
        return new OutputMessage(
                msg.getFrom(),
                msg.getText(),
                new SimpleDateFormat("HH:mm").format(new Date()));
    }
}
