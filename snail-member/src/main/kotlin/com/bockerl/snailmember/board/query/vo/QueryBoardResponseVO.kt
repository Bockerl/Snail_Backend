/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.query.vo

import com.bockerl.snailmember.board.query.enums.QueryBoardTag
import com.bockerl.snailmember.board.query.enums.QueryBoardType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable
import java.time.LocalDateTime

data class QueryBoardResponseVO(
    @field:Schema(description = "게시글 고유 번호(PK)", example = "BOA-00000001", type = "Long")
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
    @field:Schema(description = "회원번호", example = "MEM-00000001", type = "String")
    val memberId: String? = null,
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
    @field:Schema(description = "게시글 생성 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val createdAt: LocalDateTime? = null,
    @field:Schema(description = "게시글 수정 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val updatedAt: LocalDateTime? = null,
    @field:Schema(description = "게시글 파일 리스트", example = "https://-4bf8-a32b-7e4bca1e1466.png", type = "String")
    val fileList: List<String?>? = null,
) : Serializable