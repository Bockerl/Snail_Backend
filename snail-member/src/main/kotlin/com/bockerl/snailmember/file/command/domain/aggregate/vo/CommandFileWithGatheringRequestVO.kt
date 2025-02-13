package com.bockerl.snailmember.file.command.domain.aggregate.vo

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import io.swagger.v3.oas.annotations.media.Schema

data class CommandFileWithGatheringRequestVO(
    @field:Schema(description = "파일 도메인 타입", example = "BOARD", type = "FileTargetType")
    val fileTargetType: FileTargetType? = null,

    @field:Schema(description = "파일 도메인 번호", example = "1", type = "Long")
    val fileTargetId: Long,

    @field:Schema(description = "회원 번호", example = "1", type = "Long")
    val memberId: Long,

    @field:Schema(description = "모임 번호", example = "1", type = "Long")
    val gatheringId: Long,
)
