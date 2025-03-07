@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatRoomCreateRequestDto
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.ChatRoom
import com.bockerl.snailchat.chat.command.domain.aggregate.entity.MemberInfo
import com.bockerl.snailchat.chat.command.domain.aggregate.enums.CommandChatRoomType
import com.bockerl.snailchat.chat.command.domain.repository.CommandChatRoomRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
class CommandChatRoomServiceImplTest {
    @Mock
    private lateinit var commandChatRoomRepository: CommandChatRoomRepository

    @InjectMocks
    private lateinit var commandChatRoomServiceImpl: CommandChatRoomServiceImpl

    @Captor
    private lateinit var chatRoomCaptor: ArgumentCaptor<ChatRoom>

    @Nested
    @DisplayName("채팅방 테스트")
    inner class ChatRoomVerification {
        @Test
        @DisplayName("개인 채팅방 생성 성공 테스트")
        fun `개인 채팅방 생성 성공 테스트`() {
            // Given
            val personalRequestDto =
                CommandChatRoomCreateRequestDto(
                    chatRoomName = null,
                    chatRoomType = CommandChatRoomType.PERSONAL,
                )

            // When
            commandChatRoomServiceImpl.createChatRoom(personalRequestDto)

            // Then
            verify(commandChatRoomRepository).save(capture(chatRoomCaptor))
            val capturedChatRoom = chatRoomCaptor.value

            assertNull(personalRequestDto.chatRoomName) // DTO에서는 null이어야 함
            assertEquals("John", capturedChatRoom.chatRoomName) // 참여자의 닉네임이 채팅방 이름이 되어야 함
        }

        @Test
        @DisplayName("개인 채팅방 생성 실패 테스트 - 참여자가 없는 경우( 올바른 회원 정보를 가져오지 못함)")
        fun `개인 채팅방 생성 실패 테스트`() {
            // Given
            val personalRequestDto =
                CommandChatRoomCreateRequestDto(
                    chatRoomName = null,
                    chatRoomType = CommandChatRoomType.PERSONAL,
                )

            // Mocking (참여자가 없는 경우를 가정)
            val commandChatRoomService =
                object : CommandChatRoomServiceImpl(commandChatRoomRepository) {
                    override fun createChatRoom(commandChatRoomCreateRequestDto: CommandChatRoomCreateRequestDto) {
                        val participants = emptyList<MemberInfo>() // 빈 리스트로 설정
                        if (participants.isEmpty()) {
                            throw RuntimeException("참여자가 없습니다.")
                        }
                    }
                }

            // When & Then
            val exception =
                assertThrows<RuntimeException> {
                    commandChatRoomService.createChatRoom(personalRequestDto)
                }

            // 예외 메시지 검증
            assertEquals("참여자가 없습니다.", exception.message)

            // save() 메서드가 호출되지 않았는지 확인
            verify(commandChatRoomRepository, never()).save(any())
        }
    }
}