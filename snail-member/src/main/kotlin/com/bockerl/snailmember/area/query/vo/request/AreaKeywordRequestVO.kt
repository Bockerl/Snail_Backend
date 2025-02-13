package com.bockerl.snailmember.area.query.vo.request

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class AreaKeywordRequestVO(
    @field:Schema(description = "지역 검색 키워드", example = "서울", type = "String")
    @JsonProperty("area_search_keyword")
    val areaSearchKeyword: String?,
) {
    // 검증된 값만 반환하는 공개 property
    val searchKeyWord: String
        get() = validatedKeyword()

    // 글자 수 제한: 최소 - 1자, 최대 - 30자
    fun validatedKeyword(): String {
        val sqlInjectionPattern =
            buildString {
                append("(?i)(SELECT|INSERT|UPDATE|DELETE|DROP|UNION)")
            }
        return when {
            areaSearchKeyword.isNullOrBlank() ->
                throw CommonException(ErrorCode.NULL_BLANK_SEARCH_KEYWORD)

            areaSearchKeyword.length > 30 ->
                throw CommonException(ErrorCode.OVERSIZE_KEYWORD_LENGTH)

            areaSearchKeyword.contains(Regex(sqlInjectionPattern)) ->
                throw CommonException(ErrorCode.SQL_INJECTION_DETECTED)

            else -> areaSearchKeyword
        }
    }
}
