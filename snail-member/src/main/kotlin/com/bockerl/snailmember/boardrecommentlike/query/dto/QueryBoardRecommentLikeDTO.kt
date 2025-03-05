package com.bockerl.snailmember.boardrecommentlike.query.dto

import java.time.LocalDateTime

class QueryBoardRecommentLikeDTO(
    val boardRecommentLikeId: Long,
    val boardRecommendId: Long,
    val boardCommentId: Long,
    val boardId: Long,
    val memberId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    val formattedBoardRecommentLikeId: String
        get() = "BOA-REC-LIK-${boardRecommentLikeId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardRecommentId: String
        get() = "BOA-REC-${boardRecommendId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardCommentId: String
        get() = "BOA-COM-${boardCommentId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardId: String
        get() = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"

    val formatedMemberId: String
        get() = "MEM-${memberId.toString().padStart(8, '0') ?: "00000000"}"
}