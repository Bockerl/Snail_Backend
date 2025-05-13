@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.jmh.benchMark

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.openjdk.jmh.annotations.*
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.concurrent.*
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class KafkaSendBenchmark {
    private lateinit var executor: ExecutorService
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    private val topic = "benchmark-topic"
    private val payload = "benchmark-payload"

    @Setup(Level.Iteration)
    fun setup() {
        executor = Executors.newFixedThreadPool(8)

        val props =
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.ACKS_CONFIG to "1", // 빠른 전송 위해 1
                ProducerConfig.LINGER_MS_CONFIG to 1, // 약간의 지연 허용으로 batching 향상
                ProducerConfig.BATCH_SIZE_CONFIG to 32 * 1024, // 32KB
                ProducerConfig.BUFFER_MEMORY_CONFIG to 64 * 1024 * 1024, // 64MB
                ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 5,
            )

        val producerFactory = DefaultKafkaProducerFactory<String, String>(props)
        kafkaTemplate = KafkaTemplate(producerFactory)
    }

    @Benchmark
    fun sendWithExecutor() {
        val total = 1_000_000
        val latch = CountDownLatch(total)

        repeat(total) {
            executor.execute {
                try {
                    kafkaTemplate.send(topic, payload)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        kafkaTemplate.flush() // 내부 버퍼 비우기
    }

    @Benchmark
    fun sendWithCompletableFuture() {
        val total = 1_000_000
        val latch = CountDownLatch(total)

        repeat(total) {
            kafkaTemplate.send(topic, payload).whenComplete { _: SendResult<String, String>?, _: Throwable? ->
                latch.countDown()
            }
        }

        latch.await()
        kafkaTemplate.flush()
    }

    @TearDown(Level.Iteration)
    fun tearDown() {
        executor.shutdown()
        kafkaTemplate.flush()
    }
}