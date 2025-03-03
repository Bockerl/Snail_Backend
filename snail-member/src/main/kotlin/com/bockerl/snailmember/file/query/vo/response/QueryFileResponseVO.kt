/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.file.query.vo.response

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class QueryFileResponseVO(
    @field:Schema(description = "파일 고유 번호(PK)", example = "FIL-00000001", type = "String")
    val fileId: String? = null,
    @field:Schema(description = "파일 이름", example = "426e07e4bca1e1466.png", type = "String")
    val fileName: String?,
    @field:Schema(description = "파일 타입", example = "1", type = "String")
    val fileType: String?,
    @field:Schema(description = "파일 url", example = "https://bockerl.blob.core4bca1e1466.png", type = "String")
    val fileUrl: String?,
    @field:Schema(description = "활성화여부", example = "1", type = "Boolean")
    val active: Boolean,
    @field:Schema(description = "파일 타겟 도메인 타입", example = "BOARD", type = "FileTargetType")
    val fileTargetType: FileTargetType? = null,
    @field:Schema(description = "타겟 도메인 pk", example = "BOA-00000001", type = "String")
    val fileTargetId: String? = null,
    @field:Schema(description = "회원 pk", example = "MEM-00000001", type = "String")
    val memberId: String? = null,
    @field:Schema(description = "생성일시", example = "1", type = "LocalDateTime")
    val createdAt: LocalDateTime? = null,
    @field:Schema(description = "수정일시", example = "1", type = "LocalDateTime")
    val updatedAt: LocalDateTime? = null,
)
