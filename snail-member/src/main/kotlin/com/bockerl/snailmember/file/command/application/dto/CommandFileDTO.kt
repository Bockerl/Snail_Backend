/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.file.command.application.dto

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import io.swagger.v3.oas.annotations.media.Schema

data class CommandFileDTO(
    @field:Schema(description = "파일 도메인 타입", example = "BOARD", type = "FileTargetType")
    val fileTargetType: FileTargetType? = null,
    @field:Schema(description = "파일 도메인 번호", example = "1", type = "Long")
    val fileTargetId: Long? = null,
    @field:Schema(description = "회원 pk", example = "1", type = "Long")
    val memberId: Long? = null,
)