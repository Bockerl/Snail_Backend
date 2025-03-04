package com.bockerl.snailmember.area.query.vo.request

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class AreaPositionRequestVO(
    @field:Schema(description = "경도", example = "126.978275264", type = "Double")
    @JsonProperty("longitude")
    val longitude: Double?,
    @field:Schema(description = "위도", example = "37.566642192", type = "Double")
    @JsonProperty("latitude")
    val latitude: Double?,
) {
    val validLongitude: Double
        get() = validateLongitude()

    val validLatitude: Double
        get() = validateLatitude()

    fun validateLongitude(): Double =
        when {
            longitude == null -> throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
            longitude < -180.0 || longitude > 180.0 -> throw CommonException(ErrorCode.INVALID_INPUT_VALUE)
            longitude > 132 || longitude < 124 -> throw CommonException(ErrorCode.INVALID_INPUT_VALUE)
            else -> longitude
        }

    fun validateLatitude(): Double =
        when {
            latitude == null -> throw CommonException(ErrorCode.INVALID_PARAMETER_FORMAT)
            latitude < -90 || latitude > 90 -> throw CommonException(ErrorCode.INVALID_INPUT_VALUE)
            latitude > 43 || latitude < 33 -> throw CommonException(ErrorCode.INVALID_INPUT_VALUE)
            else -> latitude
        }
}