package com.bockerl.snailmember.member.command.domain.aggregate.vo.request

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ActivityAreaRegisterRequestVO(
    @field:Schema(description = "redis 저장 Id(UUID)", example = "as12f23", type = "String")
    @JsonProperty("redisId")
    val redisId: String? = null,
    @field:Schema(description = "메인 지역 PK", example = "Emd-00000001", type = "String")
    @JsonProperty("primaryId")
    val primaryFormattedId: String? = null,
    @field:Schema(description = "직장 지역", example = "Emd-00000002", type = "String")
    @JsonProperty("workplaceId")
    val workplaceFormattedId: String? = null,
) {
    val validPrimaryId: String
        get() = validatePrimaryId()

    fun validatePrimaryId(): String {
        require(
            primaryFormattedId != null &&
                (
                    primaryFormattedId.startsWith("Sid") ||
                        primaryFormattedId.startsWith("Sig") ||
                        primaryFormattedId.startsWith("Emd")
                ),
        ) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }
        return primaryFormattedId
    }
}