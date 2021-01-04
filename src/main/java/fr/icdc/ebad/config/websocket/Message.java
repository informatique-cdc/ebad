package fr.icdc.ebad.config.websocket;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Message {
    private String from;
    private String to;
    private String text;
}
