/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.file.query.repository

import com.bockerl.snailmember.file.query.dto.QueryFileDTO
import com.bockerl.snailmember.file.query.dto.QueryFileGatheringDTO
import com.bockerl.snailmember.file.query.dto.QueryFileRequestDTO
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface FileMapper {
    fun selectFilesByFileTarget(
        @Param("queryFileRequestDTO") queryFileRequestDTO: QueryFileRequestDTO,
    ): List<QueryFileDTO>

    fun selectFilesByGatheringId(
        @Param("gatheringId") gatheringId: Long,
    ): List<QueryFileGatheringDTO>
}