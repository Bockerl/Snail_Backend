package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatMessageType
import com.bockerl.snailchat.testConfig.TestSupport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.lang.reflect.Type
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CommandChatMessageServiceImplTest : TestSupport() {
    // Server_port 번호 할당
    @LocalServerPort
    private var port: Int = 0
    private val roomId = "testRoom"

    // 메시지를 저장할 메시징 큐
    private val messageQueue: BlockingQueue<CommandChatMessageRequestDto> = LinkedBlockingQueue()

    // stompClient
    private val stompClient: WebSocketStompClient =
        WebSocketStompClient(SockJsClient(listOf(WebSocketTransport(StandardWebSocketClient())))).apply {
            // 넘길 메시지를 Dto 형태로 변환
            messageConverter = MappingJackson2MessageConverter()
        }

    // STOMP 세션을 처리하는 이벤트(오류, 수신등) 핸들러
    private class StompSessionHandlerImpl : StompSessionHandlerAdapter() {
        // STOMP 메시지 오류 발생시 실행
        override fun handleFrame(
            headers: StompHeaders,
            payload: Any?,
        ) {
            println("오류 발생: $payload")
        }

        // Websocket 전송 오류 발생시 실행
        override fun handleTransportError(
            session: StompSession,
            exception: Throwable,
        ) {
            println("Transport 오류 발생: ${exception.message}")
            exception.printStackTrace()
        }
    }

    @Nested
    @DisplayName("Stomp를 통한 메시지 송수신 성공/실패 테스트")
    inner class StompVertification {
        @Test
        @DisplayName("Stomp 연결 성공 테스트")
        fun `Stomp 메시지 송수신 성공 테스트`() {
            val futureSession: CompletableFuture<StompSession> =
                stompClient.connectAsync("ws://localhost:$port/chat", StompSessionHandlerImpl())

            // 비동기 결과를 동기적으로 가져옴 (get)
            val session = futureSession.get(5, TimeUnit.SECONDS)

            // 메시지 구독
            session.subscribe(
                "/topic/message/$roomId",
                object : StompFrameHandler {
                    override fun getPayloadType(headers: StompHeaders): Type = CommandChatMessageRequestDto::class.java

                    override fun handleFrame(
                        headers: StompHeaders,
                        payload: Any?,
                    ) {
                        messageQueue.offer(payload as CommandChatMessageRequestDto)
                    }
                },
            )

            // 메시지 전송
            val messageDto =
                CommandChatMessageRequestDto(
                    sender = "Alice",
                    roomId = roomId,
                    message = "안녕하세요, Alice입니다.",
                    messageType = CommandChatMessageType.CHAT,
                )

            session.send("/app/$roomId", messageDto)

            // 메시지 정상 전달 여부 확인
            val receiveMessage = messageQueue.poll(5, TimeUnit.SECONDS)

            assertNotNull(receiveMessage, "메시지가 정상적으로 수신되지 않았습니다.")
            assertEquals("Alice", receiveMessage.sender)
            assertEquals("안녕하세요, Alice입니다.", receiveMessage.message)
        }

        @Test
        @DisplayName("Stomp 연결 실패 테스트")
        fun `Stomp 메시지 송수신 실패 테스트`() {
            val futureSession: CompletableFuture<StompSession> =
                stompClient.connectAsync("ws://localhost:$port/chat", StompSessionHandlerImpl())

            // 비동기 결과를 동기적으로 가져옴 (get)
            val session = futureSession.get(5, TimeUnit.SECONDS)

            // 메시지 구독
            session.subscribe(
                "/topic/message/$roomId",
                object : StompFrameHandler {
                    override fun getPayloadType(headers: StompHeaders): Type = CommandChatMessageRequestDto::class.java

                    override fun handleFrame(
                        headers: StompHeaders,
                        payload: Any?,
                    ) {
                        messageQueue.offer(payload as CommandChatMessageRequestDto)
                    }
                },
            )

            // 메시지 전송
            val messageDto =
                CommandChatMessageRequestDto(
                    sender = "Alice",
                    roomId = roomId,
                    message = "안녕하세요, Alice입니다.",
                    messageType = CommandChatMessageType.CHAT,
                )

            // 잘못된 경로로 전송하여 예외 발생 검증
            session.send("/chat/$roomId", messageDto)

            // 메시지 정상 전달 여부 확인
            val receiveMessage = messageQueue.poll(5, TimeUnit.SECONDS)

            // 메시지 전송 실패 검증
            assertNull(receiveMessage, "메시지가 잘못된 경로로 인해 수신되지 않아야 합니다.")
        }
    }

    @Nested
    @DisplayName("전송된 메시지 MongoDB에 저장 테스트")
    inner class MongoDBVertification {
        @Test
        @DisplayName("메시지 MongoDB에 저장 테스트")
        fun `전송하기 전 메시지 MongoDB에 저장 성공`() {
        }
    }
}