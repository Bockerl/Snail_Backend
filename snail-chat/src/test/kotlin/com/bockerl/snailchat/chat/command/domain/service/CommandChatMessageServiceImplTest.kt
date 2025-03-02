package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatMessageType
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatMessageRepository
import com.bockerl.snailchat.testConfig.TestSupport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.lang.reflect.Type
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CommandChatMessageServiceImplTest : TestSupport() {
    @Autowired
    private lateinit var mongoProperties: MongoProperties

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var commandChatMessageRepository: CommandChatMessageRepository

    // Server_port 번호 할당
    @LocalServerPort
    private var port: Int = 0

    private val chatRoomId = "testRoom"

    // 메시지를 저장할 메시징 큐
    private val messageQueue: BlockingQueue<CommandChatMessageRequestDto> = LinkedBlockingQueue()

    // stompClient
    private val stompClient: WebSocketStompClient =
        WebSocketStompClient(SockJsClient(listOf(WebSocketTransport(StandardWebSocketClient())))).apply {
            // 넘길 메시지를 Dto 형태로 변환
            messageConverter = MappingJackson2MessageConverter()
        }

    // TestContainer를 이용한 테스트를 위한 MongoDB
    companion object {
//        @Container
//        val mongoDBContainer: MongoDBContainer? =
//            // 실제 배포 환경에서는 실행되지 않도록 설정
//            if (System.getProperty("spring.profiles.active") != "prod") {
//                MongoDBContainer(DockerImageName.parse("mongo:4.4.3"))
//            } else {
//                null
//            }
        @Container
        val mongoDBContainer: MongoDBContainer =
            MongoDBContainer(DockerImageName.parse("mongo:4.4.3")).apply {
                start()
            }

        @JvmStatic // MongoDB URI 등록
        @DynamicPropertySource // 동적으로 테스트 프로퍼티 주입 ( testContainer의 MongoDB URI를 주입해주는 역할 )
        fun mongoProperties(registry: DynamicPropertyRegistry) {
            println("MongoDB Container started at: ${mongoDBContainer.replicaSetUrl}")
            registry.add("spring.data.mongodb.uri") { mongoDBContainer.replicaSetUrl }
        }
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

    @Test
    fun `MongoDB URI 확인 테스트`() {
        println("MongoDB URI: ${mongoDBContainer.replicaSetUrl}")
        assertNotNull(mongoDBContainer.replicaSetUrl, "MongoDB URI가 설정되지 않았습니다.")
    }

    @Nested
    @DisplayName("Stomp를 통한 메시지 송수신 성공/실패 테스트")
    inner class StompVertification {
        @Test
        @DisplayName("Stomp 연결 및 메시지 송수신 & 저장 성공 테스트")
        fun `Stomp 메시지 송수신 성공 테스트`() {
            val futureSession: CompletableFuture<StompSession> =
                stompClient.connectAsync("ws://localhost:$port/chat", StompSessionHandlerImpl())

            // 비동기 결과를 동기적으로 가져옴 (get)
            val session = futureSession.get(5, TimeUnit.SECONDS)

            // 메시지 구독
            session.subscribe(
                "/topic/message/$chatRoomId",
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
                    chatRoomId = chatRoomId,
                    sender = "jack",
                    message = "안녕하세요, jack입니다.",
                    messageType = CommandChatMessageType.CHAT,
                )

            // 메시지 저장
            val message =
                ChatMessage(
                    chatRoomId = messageDto.chatRoomId,
                    sender = messageDto.sender,
                    message = messageDto.message,
                    messageType = CommandChatMessageType.CHAT,
                )

            // DB에 저장
            commandChatMessageRepository.save(message)

            session.send("/app/$chatRoomId", messageDto)

//            // 메시지 정상 전달 여부 확인
//            val receiveMessage = messageQueue.poll(5, TimeUnit.SECONDS)
//
//            assertNotNull(receiveMessage, "메시지가 정상적으로 수신되지 않았습니다.")
//            assertEquals("Alice", receiveMessage.sender)
//            assertEquals("안녕하세요, Alice입니다.", receiveMessage.message)

            // TestContainer의 MongoDB에 잘 저장이 되었는 지 확인
            val savedMessage =
                mongoTemplate.findOne(
                    Query(
                        Criteria
                            .where(messageDto.chatRoomId)
                            .`is`(chatRoomId)
                            .and("sender")
                            .`is`(messageDto.sender),
                    ),
                    CommandChatMessageRequestDto::class.java,
                )

            assertNotNull(savedMessage, "MongoDB에 메시지가 저장되지 않았습니다.")
            assertEquals("Alice", savedMessage!!.sender)
            assertEquals("안녕하세요, Alice22입니다.", savedMessage.message)

            session.disconnect()
        }

        @Test
        @DisplayName("Stomp 연결 및 메시지 송수신 & 저장 실패 테스트")
        fun `Stomp 메시지 송수신 실패 테스트`() {
            val futureSession: CompletableFuture<StompSession> =
                stompClient.connectAsync("ws://localhost:$port/chat", StompSessionHandlerImpl())

            // 비동기 결과를 동기적으로 가져옴 (get)
            val session = futureSession.get(5, TimeUnit.SECONDS)

            // 메시지 구독
            session.subscribe(
                "/topic/message/$chatRoomId",
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
                    chatRoomId = chatRoomId,
                    message = "안녕하세요, Alice33입니다.",
                    messageType = CommandChatMessageType.CHAT,
                )

            // 잘못된 경로로 전송하여 예외 발생 검증
            session.send("/chattt/$chatRoomId", messageDto)

            // 메시지 정상 전달 여부 확인
            val receiveMessage = messageQueue.poll(5, TimeUnit.SECONDS)

            // 메시지 전송 실패 검증
            assertNull(receiveMessage, "메시지가 잘못된 경로로 인해 수신되지 않아야 합니다.")

            session.disconnect()
        }
    }
}