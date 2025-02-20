package com.bockerl.snailchat.chat.command.domain.service

import com.bockerl.snailchat.testConfig.TestSupport
import org.junit.jupiter.api.Assertions.*
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommandChatMessageServiceImplTest : TestSupport() {
    // Server_port 번호 할당
//    @LocalServerPort
//    private var port: Int = 0
//
//    @Autowired
//    private lateinit var commandChatMessageService: CommandChatMessageService
//
//    // WebSocketStompClient 생성
//    private fun createStompClient(): WebSocketStompClient {
//        val transports: List<Transport> = listOf(WebSocketTransport(StandardWebSocketClient()))
//        val sockJsClient = SockJsClient(transports)
//        WebSocketStompClient(sockJsClient).apply {
//            messageConverter = StringMessageConverter() // stomp 기반 Websocket으로 메세지 -> String을 위한 설정
//        }
//    }
//
//    private fun connectStompSession
//
//    @Nested
//    @DisplayName("Stomp를 통한 메시지 송수신 성공/실패 테스트")
//    inner class StompVertification() {
//        @Test
//        @DisplayName("Stomp 연결 성공 테스트")
//        fun `Stomp`
//    }
}