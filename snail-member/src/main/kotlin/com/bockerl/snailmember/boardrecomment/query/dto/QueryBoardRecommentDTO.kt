package com.bockerl.snailmember.boardrecomment.query.dto

import java.time.LocalDateTime

data class QueryBoardRecommentDTO(
    val boardRecommentId: String,
    val boardCommentId: Long,
    val boardRecommentContents: String? = null,
    val memberId: Long,
    val boardId: Long,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    val formattedId: String
        get() = "BOA-REC-${boardRecommentId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardCommentId: String
        get() = "BOA-COM-${boardCommentId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardId: String
        get() = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"

    val formatedMemberId: String
        get() = "MEM-${memberId.toString().padStart(8, '0') ?: "00000000"}"
}