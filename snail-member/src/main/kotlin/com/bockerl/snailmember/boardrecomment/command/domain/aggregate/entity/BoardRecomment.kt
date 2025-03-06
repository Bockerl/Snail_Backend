@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardrecomment.command.domain.aggregate.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "board_recomment")
data class BoardRecomment(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "boa_rec_seq_generator", // 사용할 generator 이름
    )
    @SequenceGenerator(
        name = "boa_rec_seq_generator", // generator 이름
        sequenceName = "boa_rec", // db seq 이름
        allocationSize = 1, // seq 증가량 (추후에 성능에 따라 변경해야 할지도 모름)
    )
    var boardRecommentId: Long? = null,
    @Column(name = "board_recomment_contents", columnDefinition = "TEXT")
    var boardRecommentContents: String? = null,
    @Column(name = "active", nullable = false)
    var active: Boolean = true,
    @Column(name = "member_id", nullable = false)
    var memberId: Long,
    @Column(name = "board_id", nullable = false)
    var boardId: Long,
    @Column(name = "board_comment_id", nullable = false)
    var boardCommentId: Long,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    val formattedId: String
        get() = "BOA-REC-${boardRecommentId?.toString()?.padStart(8, '0') ?: "00000000"}"
}