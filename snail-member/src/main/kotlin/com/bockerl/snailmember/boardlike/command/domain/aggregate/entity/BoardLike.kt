@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardlike.command.domain.aggregate.entity

import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardTag
import com.bockerl.snailmember.board.command.domain.aggregate.enums.BoardType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "Board_like")
data class BoardLike(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "boa_lik_seq_generator", // 사용할 generator 이름
    )
    @SequenceGenerator(
        name = "boa_lik_seq_generator", // generator 이름
        sequenceName = "boa_lik", // db seq 이름
        allocationSize = 1, // seq 증가량 (추후에 성능에 따라 변경해야 할지도 모름)
    )
    var boardLikeId: Long? = null,
    @Column(name = "board_contents", columnDefinition = "TEXT")
    var boardContents: String?,
    @Column(name = "board_type", nullable = false, length = 255)
    @Enumerated(EnumType.STRING)
    var boardType: BoardType,
    @Column(name = "board_tag", nullable = false, length = 255)
    @Enumerated(EnumType.STRING)
    var boardTag: BoardTag,
    @Column(name = "board_location", nullable = false, length = 255)
    var boardLocation: String,
    // 설명. default로 내 지역이 들어갈 수 있도록 함-> 프론트에서 처리해줄 것 (전체, 내 지역)
    @Column(name = "board_access_level", nullable = false, length = 255)
    var boardAccessLevel: String,
    @Column(name = "board_view", nullable = false, length = 255)
    var boardView: Int = 0,
    @Column(name = "active", nullable = false)
    var active: Boolean = true,
    // 설명. fk인 회원 번호
    @Column(name = "member_id", nullable = false)
    var memberId: Long? = null,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    val formattedId: String
        get() = "BOA-LIK-${boardLikeId?.toString()?.padStart(8, '0') ?: "00000000"}"
}