package com.example.email.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Gửi từ server -> client qua /topic
        config.enableSimpleBroker("/topic");

        // Client gửi tin đến server qua /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Định nghĩa endpoint WebSocket cho client kết nối
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
