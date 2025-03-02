package com.bockerl.snailchat.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration

@Configuration
@EnableWebSocketMessageBroker
class StompWebSocketConfig : WebSocketMessageBrokerConfigurer {
    // Websocket(Stomp)를 연결할 수 있는 Endpoint 설정
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/chat")
            .setAllowedOrigins("*")
            .withSockJS()
    }

    // 내부 메시지 브로커 설정
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 메시지 구독(수신) Prefix
        registry.setApplicationDestinationPrefixes("/app")
        // 메시지 발행(발신) Prefix
        registry.enableSimpleBroker("/topic")
    }

    // Websocket 통신 설정 제한
    override fun configureWebSocketTransport(registry: WebSocketTransportRegistration) {
        registry
            .setMessageSizeLimit(8192)
            .setSendTimeLimit(15 * 1000)
            .setSendBufferSizeLimit(3 * 512 * 1024)
    }
}