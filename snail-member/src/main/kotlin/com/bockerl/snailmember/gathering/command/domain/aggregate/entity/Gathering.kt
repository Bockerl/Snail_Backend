@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.gathering.command.domain.aggregate.entity

import com.bockerl.snailmember.gathering.command.domain.enums.GatheringType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "Gathering")
class Gathering(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "gat_seq_generator", // 사용할 generator 이름
    )
    @SequenceGenerator(
        name = "gat_seq_generator", // generator 이름
        sequenceName = "gat", // db seq 이름
        allocationSize = 1, // seq 증가량 (추후에 성능에 따라 변경해야 할지도 모름)
    )
    var gatheringId: Long? = null,
    @Column(name = "gathering_title", nullable = false, length = 255)
    var gatheringTitle: String?,
    @Column(name = "gathering_information", nullable = false, length = 255)
    var gatheringInformation: String?,
    @Column(name = "gathering_type", nullable = false, length = 255)
    @Enumerated(EnumType.STRING)
    var gatheringType: GatheringType,
    // 설명. default로 내 지역이 들어갈 수 있도록 함-> 프론트에서 처리해줄 것 (전체, 내 지역)
    @Column(name = "gathering_region", nullable = false, length = 255)
    var gatheringRegion: String,
    @Column(name = "gathering_limit", nullable = false)
    var gatheringLimit: Int = 50,
    @Column(name = "active", nullable = false)
    var active: Boolean = true,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    val formattedId: String
        get() = "GAT-${gatheringId?.toString()?.padStart(8, '0') ?: "00000000"}"
}