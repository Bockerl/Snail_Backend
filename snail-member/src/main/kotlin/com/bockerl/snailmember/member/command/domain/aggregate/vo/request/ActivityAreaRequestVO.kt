package com.bockerl.snailmember.member.command.domain.aggregate.vo.request

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ActivityAreaRequestVO(
    @field:Schema(description = "회원 PK", example = "Mem-00000001", type = "String")
    @JsonProperty("memberId")
    val memberId: String? = null,
    @field:Schema(description = "메인 지역 PK", example = "Emd-00000001", type = "String")
    @JsonProperty("primaryId")
    val primaryId: String? = null,
    @field:Schema(description = "직장 지역", example = "Emd-00000002", type = "String")
    @JsonProperty("workplaceId")
    val workplaceId: String? = null,
) {
    val validMemberId: String
        get() = validateMemberId()

    val validPrimaryId: String
        get() = validatePrimaryId()

    fun validateMemberId(): String {
        require(
            !memberId.isNullOrBlank() && memberId.startsWith("MEM"),
        ) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }
        return memberId
    }

    fun validatePrimaryId(): String {
        require(
            !primaryId.isNullOrBlank() && primaryId.startsWith("EMD"),
        ) {
            throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
        }
        return primaryId
    }
}