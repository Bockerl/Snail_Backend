package com.bockerl.snailchat.infrastructure.producer

import com.bockerl.snailchat.chat.command.application.dto.request.CommandChatMessageRequestDTO
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class KafkaMessageProducerImpl(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) : KafkaMessageProducer {
    private val logger = KotlinLogging.logger { }

    /**
     * 지정된 토픽에 메시지를 전송합니다.
     * @param topic 메시지를 전송할 Kafka 토픽
     * @param key 메시지 파티셔닝 및 순서를 위한 키 (여기서는 채팅방 ID 사용)
     * @param chatMessageDTO 전송할 메시지 객체 (CommandChatMessageRequestDto)
     */
    override fun sendMessageByKafka(
        topic: String,
        key: String,
        chatMessageDTO: CommandChatMessageRequestDTO,
    ) {
        logger.info { "Kafka로 메시지 전송: 토픽=$topic, key=$key, message=$chatMessageDTO" }

        val future: CompletableFuture<SendResult<String, Any>> =
            // Kafka로 메시지 전송 ( 비동기 통신은 try-catch보다 completableFuture 사용)
            kafkaTemplate.send(topic, key, chatMessageDTO)
        future.whenComplete { result: SendResult<String, Any>?, ex: Throwable? ->
            if (ex != null) {
                logger.error(ex) { "Kafka 토픽 [$topic] 에 메시지 전송 실패" }
            } else {
                logger.info {
                    "메시지 전송 성공: partition=${result?.recordMetadata?.partition()}, offset=${result?.recordMetadata?.offset()}"
                }
            }
        }
    }
}