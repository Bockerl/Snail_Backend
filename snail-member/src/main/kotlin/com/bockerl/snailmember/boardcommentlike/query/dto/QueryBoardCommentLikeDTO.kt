package com.bockerl.snailmember.boardcommentlike.query.dto

import java.time.LocalDateTime

class QueryBoardCommentLikeDTO(
    val boardCommentLikeId: Long,
    val boardCommentId: Long,
    val boardId: Long,
    val memberId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    val formattedBoardCommentLikeId: String
        get() = "BOA-COM-LIK-${boardCommentLikeId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardCommentId: String
        get() = "BOA-COM-${boardCommentId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardId: String
        get() = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"

    val formatedMemberId: String
        get() = "MEM-${memberId.toString().padStart(8, '0') ?: "00000000"}"
}