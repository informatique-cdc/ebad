package fr.icdc.ebad.config.websocket;

import fr.icdc.ebad.security.jwt.TokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    private final TokenProvider tokenProvider;
    private final ResourceServerTokenServices resourceServerTokenServices;
    private final Environment environment;

    public WebsocketConfig(TokenProvider tokenProvider, @Nullable ResourceServerTokenServices resourceServerTokenServices, Environment environment) {
        this.tokenProvider = tokenProvider;
        this.resourceServerTokenServices = resourceServerTokenServices;
        this.environment = environment;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(org.springframework.messaging.Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                    return message;
                }

                List<String> tokenList = accessor.getNativeHeader("Authorization");
                String token;
                if (tokenList == null || tokenList.isEmpty()) {
                    return message;
                }

                token = tokenList.get(0);
                if (token == null) {
                    return message;
                }

                Authentication authentication = null;
                if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("jwt")))) {
                    authentication = tokenProvider.getAuthentication(token);
                } else if (resourceServerTokenServices != null) {
                    authentication = resourceServerTokenServices.loadAuthentication(token);
                }
                accessor.setUser(authentication);
                return message;
            }
        });
    }
}
