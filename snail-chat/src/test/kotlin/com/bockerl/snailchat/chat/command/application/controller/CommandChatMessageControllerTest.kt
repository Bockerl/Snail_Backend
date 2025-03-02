package com.bockerl.snailchat.chat.command.application.controller

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDto
import com.bockerl.snailchat.chat.command.application.mapper.VoToDtoConverter
import com.bockerl.snailchat.chat.command.application.service.CommandChatMessageService
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatMessageType
import com.bockerl.snailchat.chat.command.domain.aggregate.vo.request.SendMessageRequestVo
import com.bockerl.snailchat.testConfig.TestConfiguration
import com.bockerl.snailchat.testConfig.TestSupport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.messaging.simp.SimpMessageHeaderAccessor

@Import(TestConfiguration::class)
class CommandChatMessageControllerTest : TestSupport() {
    @Autowired
    private lateinit var commandChatMessageService: CommandChatMessageService

    @Autowired
    private lateinit var voToDtoConverter: VoToDtoConverter

    @Autowired
    private lateinit var commandChatMessageController: CommandChatMessageController

    @Nested
    @DisplayName("메세지 Type에 따른 세션 동작 테스트")
    inner class SimpMessageHeaderAccessorVeritification {
        @Test
        @DisplayName("첫 입장시 세션 정보 동작 테스트")
        fun `ENTER 메시지를 받으면 세션에 사용자 정보 저장()`() {
            // Given
            val sender = "Alice"
            val sendMessageRequestVo =
                SendMessageRequestVo(
                    chatRoomId = "room-00001",
                    sender = sender,
                    message = "",
                    messageType = CommandChatMessageType.ENTER,
                )

            val chatRoomId = sendMessageRequestVo.chatRoomId

            // 세션 생성 및 초기화
            val mockHeaderAccessor = SimpMessageHeaderAccessor.create()
            mockHeaderAccessor.setSessionAttributes(mutableMapOf())

            // voToDtoConverter 메소드를 실제로 실행하진 않고, mock으로 미리 지정해둠
            val mockDto =
                CommandChatMessageRequestDto(
                    chatRoomId = chatRoomId,
                    sender = sender,
                    message = "",
                    messageType = CommandChatMessageType.ENTER,
                )

            // voToDtoConverter가 실행되어야 하는 구간에 실행 되었다고 가정하고 mockDto를 반환
            whenever(voToDtoConverter.sendMessageRequestVoToDto(sendMessageRequestVo, chatRoomId)).thenReturn(mockDto)

            // When
            commandChatMessageController.sendMessage(chatRoomId, sendMessageRequestVo, mockHeaderAccessor)

            // Then
            // 1. 세션에 username, chatRoomId가 잘 저장되어는지 확인
            val sessionAttributes = mockHeaderAccessor.sessionAttributes
            assertNotNull(sessionAttributes)
            assertEquals(sender, sessionAttributes?.get("username"))
            assertEquals(chatRoomId, sessionAttributes?.get("chatRoomId"))

            // 2. 세션에 저장된 후 message가 잘 변경되었는지
            val expectedMessage = "${sender}님이 입장하셨습니다."
            verify(commandChatMessageService).sendMessage(chatRoomId, mockDto.copy(message = expectedMessage))
        }

        @Test
        @DisplayName("첫 입장 아닐시 세션 무동작 테스트")
        fun `CHAT 메시지를 받으면 세션 동작하지 않음()`() {
            // Given
            val sender = "Alice"
            val sendMessageRequestVo =
                SendMessageRequestVo(
                    chatRoomId = "room-00001",
                    sender = sender,
                    message = "안녕하세요",
                    messageType = CommandChatMessageType.CHAT,
                )

            val chatRoomId = sendMessageRequestVo.chatRoomId

            // 세션 생성 및 초기화 -> Enter 후에 Chat이 일어나기 때문에, 세션은 빈 객체에서 변경이 일어나지 않는다.
            val mockHeaderAccessor = SimpMessageHeaderAccessor.create()
            mockHeaderAccessor.setSessionAttributes(mutableMapOf())

            // voToDtoConverter 메소드를 실제로 실행하진 않고, mock으로 미리 지정해둠
            val mockDto =
                CommandChatMessageRequestDto(
                    chatRoomId = chatRoomId,
                    sender = sender,
                    message = "안녕하세요",
                    messageType = CommandChatMessageType.CHAT,
                )

            // voToDtoConverter가 실행되어야 하는 구간에 실행 되었다고 가정하고 mockDto를 반환
            whenever(voToDtoConverter.sendMessageRequestVoToDto(sendMessageRequestVo, chatRoomId)).thenReturn(mockDto)

            // When
            commandChatMessageController.sendMessage(chatRoomId, sendMessageRequestVo, mockHeaderAccessor)

            // Then
            // 1. 세션이 변경되지 않았음을 확인
            val sessionAttributes = mockHeaderAccessor.sessionAttributes
            assertNotNull(sessionAttributes)
            assertTrue(sessionAttributes!!.isEmpty()) // 세션에 값이 없어야 한다.

            // 2. 전송된 메시지가 원래 메시지 그대로인지 확인
            verify(commandChatMessageService).sendMessage(chatRoomId, mockDto) // 메시지 변경 없이 전송되어야 한다.

            // 3. expectedMessage ("Alice님이 입장하셨습니다.")와 같은 메시지로 변경되지 않았는지 확인
            val expectedMessage = "${sender}님이 입장하셨습니다."
            assertNotEquals(expectedMessage, mockDto.message) // 메시지는 변경되면 안 된다.
        }
    }
}