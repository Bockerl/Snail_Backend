package com.bockerl.snailmember.gathering.query.vo

import com.bockerl.snailmember.gathering.query.enums.QueryGatheringType
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable
import java.time.LocalDateTime

data class QueryGatheringResponseVO(
    @field:Schema(description = "모임 고유 번호(PK)", example = "GAT-00000001", type = "String")
    val gatheringId: String? = null,
    @field:Schema(description = "모임 정보", example = "달팽이 좋아해요?", type = "String")
    val gatheringInformation: String? = null,
    @field:Schema(description = "모임 제목", example = "달팽이", type = "String")
    val gatheringTitle: String? = null,
    @field:Schema(description = "모임 타입", example = "HOBBY", type = "String")
    val queryGatheringType: QueryGatheringType? = null,
    @field:Schema(description = "모임 지역", example = "신대방", type = "String")
    val gatheringRegion: String? = null,
    @field:Schema(description = "게시글 활성화 여부", example = "true", type = "String")
    val active: Boolean? = null,
    @field:Schema(
        description = "회원 사진",
        example = "https://-4bf8-a32b-7e4bca1e1466.png",
        type = "String",
    )
    @JsonProperty(namespace = "memberPhoto")
    val memberPhoto: String? = null,
    @field:Schema(description = "게시글 생성 시각", example = "2024-12-11 14:45:30", type = "LocalDateTime")
    val createdAt: LocalDateTime? = null,
) : Serializable