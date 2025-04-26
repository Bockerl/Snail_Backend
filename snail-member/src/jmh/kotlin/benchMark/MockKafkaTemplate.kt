package com.bockerl.snailmember.jmh.benchMark

import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.CompletableFuture

class MockKafkaTemplate :
    KafkaTemplate<String, String>(
        DefaultKafkaProducerFactory(emptyMap()),
    ) {
    override fun send(
        topic: String,
        data: String,
    ): CompletableFuture<SendResult<String, String>> {
        val future = CompletableFuture<SendResult<String, String>>()
        val metadata =
            RecordMetadata(
                TopicPartition(topic, 0),
                0,
                0,
                System.currentTimeMillis(),
                0L,
                0,
                0,
            )
        val result = SendResult<String, String>(null, metadata)
        future.complete(result)
        return future
    }
}