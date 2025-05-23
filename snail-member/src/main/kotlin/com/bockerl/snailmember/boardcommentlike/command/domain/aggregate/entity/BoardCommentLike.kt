@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(
    name = "board_comment_like",
    indexes = [
        Index(
            name = "board_comment_like_member_board_comment",
            columnList = "memberId, boardCommentId",
            unique = true,
        ),
    ],
)
data class BoardCommentLike(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "boa_com_lik_seq_generator", // 사용할 generator 이름
    )
    @SequenceGenerator(
        name = "boa_com_lik_seq_generator", // generator 이름
        sequenceName = "boa_com_lik", // db seq 이름
        allocationSize = 1, // seq 증가량 (추후에 성능에 따라 변경해야 할지도 모름)
    )
    val boardCommentLikeId: Long? = null,
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    @Column(name = "board_id", nullable = false)
    val boardId: Long,
    @Column(name = "board_comment_id", nullable = false)
    val boardCommentId: Long,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: java.time.LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: java.time.LocalDateTime

    val formattedId: String
        get() = "BOA-COM-LIK-${boardCommentLikeId?.toString()?.padStart(8, '0') ?: "00000000"}"
}