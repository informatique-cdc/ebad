package fr.icdc.ebad.config.websocket;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
@ComponentScan("fr.icdc.ebad.web.rest")
public class SocketBrokerConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    public static final String SECURED_CHAT_HISTORY = "/secured/history";
    public static final String SECURED_CHAT = "/secured/chat";
    public static final String SECURED_CHAT_ROOM = "/secured/room";
    public static final String SECURED_CHAT_SPECIFIC_USER = "/secured/user/queue/specific-user";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(SECURED_CHAT_HISTORY, SECURED_CHAT_SPECIFIC_USER);
        config.setApplicationDestinationPrefixes("/ebad");
        config.setUserDestinationPrefix("/secured/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(SECURED_CHAT_ROOM).setAllowedOrigins("*").withSockJS();
        registry.addEndpoint(SECURED_CHAT).setAllowedOrigins("*").withSockJS();
    }

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpDestMatchers("/secured/**", "/secured/**/**").permitAll()
                .anyMessage().permitAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
