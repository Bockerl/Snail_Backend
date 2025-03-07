@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatMessage
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatMessageType
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatMessageRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.messaging.simp.SimpMessagingTemplate
import kotlin.test.Test

// @Testcontainers
@ExtendWith(MockitoExtension::class)
class CommandChatMessageServiceImplTest {
    // Mock 객체 설정
    @Mock
    private lateinit var simpleMessagingTemplate: SimpMessagingTemplate

    @Mock
    private lateinit var commandChatMessageRepository: CommandChatMessageRepository

    @InjectMocks
    private lateinit var commandChatMessageService: CommandChatMessageServiceImpl

    // ArgumentCaptor를 사용하여 전송된 메시지를 확인하기 위해 추가
    @Captor
    private lateinit var messageCaptor: ArgumentCaptor<CommandChatMessageRequestDto>

    @Captor
    private lateinit var chatMessageCaptor: ArgumentCaptor<ChatMessage>

    // URL에서 추출할 chatRoomId 미리 지정
    private val chatRoomId = "testRoom"

    @Nested
    @DisplayName("Stomp를 통한 메시지 송수신 성공 테스트")
    inner class StompVerification {
        @Test
        @DisplayName("Stomp 메시지 송수신 성공 테스트")
        fun `Stomp 메시지 송수신 성공 테스트`() {
            // Given: 테스트 메시지 생성
            val messageDto =
                CommandChatMessageRequestDto(
                    chatRoomId = chatRoomId,
                    sender = "jack",
                    message = "안녕하세요, jack입니다.",
                    messageType = CommandChatMessageType.CHAT,
                )

            val chatMessage =
                ChatMessage(
                    chatRoomId = chatRoomId,
                    sender = messageDto.sender,
                    message = messageDto.message,
                    messageType = messageDto.messageType,
                )

            // chatMessageRepository.save()가 chatMessage를 반환하도록 Mock 설정
            `when`(commandChatMessageRepository.save(any())).thenReturn(chatMessage)

            // When: sendMessage() 호출
            commandChatMessageService.sendMessage(chatRoomId, messageDto)

            // Then: save() 메서드가 1회 호출되었는지 검증 + DB에 저장하고자 하는 entity 확인
            verify(commandChatMessageRepository, times(1)).save(chatMessageCaptor.capture())

            val savedMessage = chatMessageCaptor.value
            assertNotNull(savedMessage, "채팅 메시지가 저장되지 않았습니다.")
            assertEquals("jack", savedMessage.sender)
            assertEquals("안녕하세요, jack입니다.", savedMessage.message)

            // Then: 메시지가 Stomp를 통해 전송되었는지 검증
            verify(simpleMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/message/$chatRoomId"), messageCaptor.capture())

            val sentMessage = messageCaptor.value
            assertNotNull(sentMessage, "STOMP 메시지가 전송되지 않았습니다.")
            assertEquals("jack", sentMessage.sender)
            assertEquals("안녕하세요, jack입니다.", sentMessage.message)
        }

        @Test
        @DisplayName("Stomp 메시지 송수신 실패 테스트 - Stomp 전송 실패")
        fun `Stomp 메시지 송수신 실패 테스트 - Stomp 전송 실패`() {
            // Given: 잘못된 STOMP 경로
            val invalidDestination = "/topic/mmeessaagge/$chatRoomId"

            // Given: 테스트 메시지 생성
            val messageDto =
                CommandChatMessageRequestDto(
                    chatRoomId = chatRoomId,
                    sender = "jack",
                    message = "안녕하세요, jack입니다.",
                    messageType = CommandChatMessageType.CHAT,
                )

            //  STOMP 메시지 전송 시 예외 발생하도록 Mock 설정
            doThrow(RuntimeException("잘못된 STOMP 경로로 인한 전송 오류"))
                .`when`(simpleMessagingTemplate)
                .convertAndSend(any<String>(), any<CommandChatMessageRequestDto>())

            // When & Then: 존재하지 않는 STOMP 경로로 전송 시 예외 발생 확인
            val exception =
                assertThrows<RuntimeException> {
                    simpleMessagingTemplate.convertAndSend(invalidDestination, messageDto)
                }

            // Then: 예외 메시지 검증
            assertEquals("잘못된 STOMP 경로로 인한 전송 오류", exception.message)

            // Then: STOMP 메시지 전송이 시도되었는지 검증
            verify(simpleMessagingTemplate, times(1))
                .convertAndSend(eq(invalidDestination), any<CommandChatMessageRequestDto>())
        }
    }
}

// 통합 테스트시 Test Container 적용할 예정
//    @Autowired
//    private lateinit var mongoProperties: MongoProperties
//    @Autowired
//    private lateinit var mongoTemplate: MongoTemplate
// TestContainer를 이용한 테스트를 위한 MongoDB
//    companion object {
//        @Container
//        val mongoDBContainer: MongoDBContainer? =
//            // 실제 배포 환경에서는 실행되지 않도록 설정
//            if (System.getProperty("spring.profiles.active") != "prod") {
//                MongoDBContainer(DockerImageName.parse("mongo:4.4.3"))
//            } else {
//                null
//            }
//
//        @JvmStatic // MongoDB URI 등록
//        @DynamicPropertySource // 동적으로 테스트 프로퍼티 주입 ( testContainer의 MongoDB URI를 주입해주는 역할 )
//        fun mongoProperties(registry: DynamicPropertyRegistry) {
//            println("MongoDB Container started at: ${mongoDBContainer?.replicaSetUrl}")
//            registry.add("spring.data.mongodb.uri") { mongoDBContainer?.replicaSetUrl }
//        }
//    }