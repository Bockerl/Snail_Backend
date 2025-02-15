@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardlike.command.domain.aggregate.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "board-like")
// 설명. 복합키 인덱스 설정해서 중복 삽입 시 예외 발생 설정
@CompoundIndex(name = "board-like-id:index", def = "{'boardId': 1, 'memberId': 1}", unique = true)
data class BoardLike(
    // 설명. id는 자동으로 생성됨
    @Id
    val boardLikeId: String? = null,
    val memberId: String,
    val boardId: String,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}