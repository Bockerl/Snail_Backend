package com.bockerl.snailmember.blob.command.domain.aggregate.vo

import com.bockerl.snailmember.blob.command.domain.aggregate.enums.FileTargetType
import io.swagger.v3.oas.annotations.media.Schema

data class CommandFileRequestVO(
    @field:Schema(description = "파일 타겟 도메인 타입", example = "BOARD", type = "FileTargetType")
    val fileTargetType: FileTargetType? = null,

    @field:Schema(description = "타겟 도메인 pk", example = "1", type = "Long")
    val fileTargetId: Long? = null,

)