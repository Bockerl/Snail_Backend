package com.bockerl.snailmember.file.command.application.dto

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import io.swagger.v3.oas.annotations.media.Schema

class CommandFileWithGatheringDTO(
    @field:Schema(description = "파일 도메인 타입", example = "BOARD", type = "FileTargetType")
    val fileTargetType: FileTargetType,
    @field:Schema(description = "파일 도메인 번호", example = "BOA-00000001", type = "String")
    val fileTargetId: String,
    @field:Schema(description = "회원 번호", example = "MEM-00000001", type = "String")
    val memberId: String,
    @field:Schema(description = "모임 번호", example = "GAT-00000001", type = "String")
    val gatheringId: String,
)