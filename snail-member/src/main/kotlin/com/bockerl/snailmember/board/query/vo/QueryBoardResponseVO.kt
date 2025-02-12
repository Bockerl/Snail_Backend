package com.bockerl.snailmember.board.query.vo

import com.bockerl.snailmember.board.query.enums.QueryBoardTag
import com.bockerl.snailmember.board.query.enums.QueryBoardType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class QueryBoardResponseVO(
    @field:Schema(description = "게시글 고유 번호(PK)", example = "1", type = "Long")
    val boardId: String? = null,
    @field:Schema(description = "게시글 내용", example = "달팽이 좋아요", type = "String")
    val boardContents: String? = null,
    @field:Schema(description = "게시글 타입", example = "FREE", type = "String")
    val queryBoardType: QueryBoardType? = null,
    @field:Schema(description = "게시글 태그", example = "TIP", type = "String")
    val queryBoardTag: QueryBoardTag? = null,
    @field:Schema(description = "게시글 지역", example = "SINDAEBANG", type = "String")
    val boardLocation: String? = null,
    @field:Schema(description = "게시글 공개 범위", example = "ALL", type = "String")
    val boardAccessLevel: String? = null,
    @field:Schema(description = "게시글 조회수", example = "1", type = "Int")
    val boardView: Int? = null,
    @field:Schema(description = "게시글 활성화 여부", example = "true", type = "String")
    val active: Boolean? = null,
    @field:Schema(description = "회원번호", example = "1", type = "Long")
    val memberId: Long? = null,
    @field:Schema(description = "게시글 생성 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val createdAt: LocalDateTime? = null,
    @field:Schema(description = "게시글 수정 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val updatedAt: LocalDateTime? = null,
)