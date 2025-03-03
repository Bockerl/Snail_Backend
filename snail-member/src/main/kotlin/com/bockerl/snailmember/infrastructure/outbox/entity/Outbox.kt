@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.infrastructure.outbox.entity

import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "Outbox", indexes = [Index(name = "idx_event_id", columnList = "event_id")])
class Outbox(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "out_seq_generator", // 사용할 generator 이름
    )
    @SequenceGenerator(
        name = "out_seq_generator", // generator 이름
        sequenceName = "out", // db seq 이름
        allocationSize = 1, // seq 증가량 (추후에 성능에 따라 변경해야 할지도 모름)
    )
    var outboxId: Long? = null,
    @Column(name = "event_id", unique = true, nullable = false)
    var eventId: Long? = null,
    @Column(name = "aggregate_id", nullable = false)
    var aggregateId: Long?,
    @Column(name = "event_type", nullable = false, length = 255)
    @Enumerated(EnumType.STRING)
    var eventType: EventType,
    @JsonProperty("payload")
    @Column(name = "payload", columnDefinition = "TEXT")
    var payload: String,
    @Column(name = "status", nullable = false)
    var status: String = "PENDING",
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    val formattedId: String
        get() = "OUT-${outboxId?.toString()?.padStart(8, '0') ?: "00000000"}"
}