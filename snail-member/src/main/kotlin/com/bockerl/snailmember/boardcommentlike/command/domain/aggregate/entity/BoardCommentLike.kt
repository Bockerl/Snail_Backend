@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity

import jakarta.persistence.*
import kotlinx.datetime.LocalDateTime
import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "board-comment-like")
// 설명. 복합키 인덱스 설정해서 중복 삽입 시 예외 발생 설정
@CompoundIndex(name = "board-comment-like-id:index", def = "{'boardId': 1, 'memberId': 1}", unique = true)
data class BoardCommentLike(
    // 설명. id는 자동으로 생성됨
    @Id
    val boardCommentLikeId: String? = null,
    val memberId: String,
    @Indexed
    val boardId: String,
    @Indexed
    val boardCommentId: String,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime
}