@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardlike.command.domain.aggregate.entity

import jakarta.persistence.*
import kotlinx.datetime.LocalDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

// @Document(collection = "board-like")
// 설명. 복합키 인덱스 설정해서 중복 삽입 시 예외 발생 설정
// @CompoundIndex(name = "board-like-id:index", def = "{'boardId': 1, 'memberId': 1}", unique = true)
@Entity
@Table(name = "Board-like")
data class BoardLike(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "boa_lik_seq_generator", // 사용할 generator 이름
    )
    @SequenceGenerator(
        name = "boa_like_seq_generator", // generator 이름
        sequenceName = "boa_lik", // db seq 이름
        allocationSize = 1, // seq 증가량 (추후에 성능에 따라 변경해야 할지도 모름)
    )
    var boardLikeId: Long? = null,
    // 설명. fk인 회원 번호
    @Column(name = "member_id", nullable = false)
    var memberId: Long,
    @Column(name = "board_id", nullable = false)
    var boardId: Long,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: java.time.LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: java.time.LocalDateTime

    val formattedId: String
        get() = "BOA-LIK-${boardLikeId?.toString()?.padStart(8, '0') ?: "00000000"}"
}