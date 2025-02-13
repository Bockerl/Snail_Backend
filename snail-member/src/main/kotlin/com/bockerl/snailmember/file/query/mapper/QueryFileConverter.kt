/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.file.query.mapper

import com.bockerl.snailmember.file.query.dto.QueryFileDTO
import com.bockerl.snailmember.file.query.dto.QueryFileGatheringDTO
import com.bockerl.snailmember.file.query.vo.response.QueryFileGatheringResponseVO
import com.bockerl.snailmember.file.query.vo.response.QueryFileResponseVO
import org.springframework.stereotype.Component

@Component
class QueryFileConverter {
    fun dtoToResponseVO(dto: QueryFileDTO): QueryFileResponseVO =
        QueryFileResponseVO(
            fileId = dto.formattedId,
            fileName = dto.fileName,
            fileType = dto.fileType,
            fileUrl = dto.fileUrl,
            active = dto.active,
            memberId = dto.memberId,
            fileTargetType = dto.fileTargetType,
            fileTargetId = dto.fileTargetId,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
        )

    fun gatheringDTOToResponseVO(dto: QueryFileGatheringDTO): QueryFileGatheringResponseVO =
        QueryFileGatheringResponseVO(
            fileId = dto.formattedId,
            fileName = dto.fileName,
            fileType = dto.fileType,
            fileUrl = dto.fileUrl,
            active = dto.active,
            memberId = dto.memberId,
            fileTargetType = dto.fileTargetType,
            fileTargetId = dto.fileTargetId,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            gatheringId = dto.gatheringId,
        )
}