package com.bockerl.snailmember.boardcomment.query.vo

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class QueryBoardCommentResponseVO(
    @field:Schema(description = "게시글 댓글 고유 번호(PK)", example = "BOA-COM-00000001", type = "String")
    val boardCommentId: String? = null,
    @field:Schema(description = "게시글 댓글 내용", example = "달팽이 좋아요", type = "String")
    val boardCommentContents: String? = null,
    @field:Schema(description = "회원번호", example = "MEM-00000001", type = "String")
    val memberId: String? = null,
    @field:Schema(description = "게시글 고유 번호(PK)", example = "BOA-00000001", type = "Long")
    val boardId: String? = null,
    @field:Schema(description = "게시글 댓글 활성화 여부", example = "true", type = "String")
    val active: Boolean? = null,
    @field:Schema(description = "게시글 댓글 생성 시각", example = "2024-12-11 14:45:30", type = "String")
    val createdAt: LocalDateTime? = null,
    @field:Schema(description = "게시글 댓글수정 시각", example = "2024-12-11 14:45:30", type = "String")
    val updatedAt: LocalDateTime? = null,
    @field:Schema(
        description = "회원 사진",
        example = "https://-4bf8-a32b-7e4bca1e1466.png",
        type = "String",
    )
    val memberPhoto: String? = null,
    @field:Schema(
        description = "회원 닉네임",
        example = "개똥이",
        type = "String",
    )
    val memberNickname: String? = null,
    @field:Schema(
        description = "게시글 댓글 gif",
        example = "https://-4bf8-a32b-7e4bca1e1466.png",
        type = "String",
    )
    val boardCommentGif: String,
) {
//    @field:Schema(
//        description = "게시글 댓글 gif",
//        example = "https://-4bf8-a32b-7e4bca1e1466.png",
//        type = "String",
//    )
//    private lateinit var boardCommentGif: String
}