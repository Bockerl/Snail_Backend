package com.bockerl.snailmember.boardcomment.query.dto

import java.time.LocalDateTime

data class QueryBoardCommentDTO(
    val boardCommentId: Long,
    // 설명. null일 수도 있음
    val boardCommentContents: String? = null,
    val memberId: Long,
    val boardId: Long,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    val formattedId: String
        get() = "BOA-COM-${boardCommentId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardId: String
        get() = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"

    val formatedMemberId: String
        get() = "MEM-${memberId.toString().padStart(8, '0') ?: "00000000"}"
}