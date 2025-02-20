@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.config

import com.bockerl.snailchat.testConfig.TestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.converter.StringMessageConverter
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.Test

// 어플리케이션 컨텍스트를 로드하여 실제 내장 서버를 실행 (Websocket 연결 테스트는 실제 서버 실행 필요)
// 무작위 포트를 배정하여, 실제 실행된 서버의 포트와 충돌 방지
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompWebSocketConfigTest : TestSupport() {
    // Server_port 번호 할당
    @LocalServerPort
    private var port: Int = 0

    // WebSocketStompClient를 lazy로 생성하여 필요할 때만 초기화
    private val stompClient: WebSocketStompClient by lazy {
        val transports: List<Transport> = listOf(WebSocketTransport(StandardWebSocketClient()))
        val sockJsClient = SockJsClient(transports)
        WebSocketStompClient(sockJsClient).apply {
            messageConverter = StringMessageConverter() // stomp 기반 Websocket으로 메세지 -> String을 위한 설정
        }
    }

    @Nested
    @DisplayName("Websocket 연결 성공/실패 테스트")
    inner class WebsocketVerification {
        @Test
        @DisplayName("Websocket 연결 성공 테스트")
        fun `올바른 URL로 WebSocket 연결 테스트`() {
            val futureSession: CompletableFuture<StompSession> =
                stompClient.connectAsync("ws://localhost:$port/chat", object : StompSessionHandlerAdapter() {})

            val session = futureSession.get(5, TimeUnit.SECONDS)

            assertThat(session.isConnected).isTrue()
        }

        @Test
        @DisplayName("Websocket 연결 실패 테스트")
        fun `잘못된 URL로 WebSocket 연결 테스트`() {
            val futureSession: CompletableFuture<StompSession> =
                stompClient.connectAsync("ws://localhost:$port/invalid", object : StompSessionHandlerAdapter() {})

            try {
                futureSession.get(3, TimeUnit.SECONDS)
            } catch (e: Exception) {
                assertThat(e).isInstanceOf(Exception::class.java)
            }
        }
    }
}