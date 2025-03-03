package com.bockerl.snailmember.boardlike.query.dto

import java.time.LocalDateTime

class QueryBoardLikeDTO(
    val boardLikeId: Long,
    val memberId: Long,
    val boardId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    val formattedId: String
        get() = "BOA-LIK-${boardLikeId.toString().padStart(8, '0') ?: "00000000"}"

    val formattedBoardId: String
        get() = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"

    val formatedMemberId: String
        get() = "MEM-${memberId.toString().padStart(8, '0') ?: "00000000"}"
}