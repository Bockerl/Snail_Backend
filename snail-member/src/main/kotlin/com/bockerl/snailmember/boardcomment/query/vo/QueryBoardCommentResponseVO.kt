package com.bockerl.snailmember.boardcomment.query.vo

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable
import java.time.LocalDateTime

data class QueryBoardCommentResponseVO(
    @field:Schema(description = "게시글 댓글 고유 번호(PK)", example = "BOA-COM-00000001", type = "String")
    @JsonProperty(namespace = "boardCommentId")
    val boardCommentId: String? = null,
    @field:Schema(description = "게시글 댓글 내용", example = "달팽이 좋아요", type = "String")
    @JsonProperty(namespace = "boardCommentContents")
    val boardCommentContents: String? = null,
    @field:Schema(description = "회원번호", example = "MEM-00000001", type = "String")
    @JsonProperty(namespace = "memberId")
    val memberId: String? = null,
    @field:Schema(description = "게시글 고유 번호(PK)", example = "BOA-00000001", type = "Long")
    @JsonProperty(namespace = "boardId")
    val boardId: String? = null,
    @field:Schema(description = "게시글 댓글 활성화 여부", example = "true", type = "Boolean")
    @JsonProperty(namespace = "active")
    val active: Boolean? = null,
    @field:Schema(description = "게시글 댓글 생성 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    @JsonProperty(namespace = "createdAt")
    val createdAt: LocalDateTime? = null,
    @field:Schema(description = "게시글 댓글수정 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    @JsonProperty(namespace = "updatedAt")
    val updatedAt: LocalDateTime? = null,
    @field:Schema(
        description = "회원 사진",
        example = "https://-4bf8-a32b-7e4bca1e1466.png",
        type = "String",
    )
    @JsonProperty(namespace = "memberPhoto")
    val memberPhoto: String? = null,
    @field:Schema(
        description = "회원 닉네임",
        example = "개똥이",
        type = "String",
    )
    @JsonProperty(namespace = "memberNickname")
    val memberNickname: String? = null,
    @field:Schema(
        description = "게시글 댓글 gif",
        example = "https://-4bf8-a32b-7e4bca1e1466.png",
        type = "String",
    )
    @JsonProperty(namespace = "boardCommentGif")
    val boardCommentGif: String? = "",
) : Serializable