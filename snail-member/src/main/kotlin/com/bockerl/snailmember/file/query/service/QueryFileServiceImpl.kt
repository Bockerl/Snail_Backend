/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.file.query.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.query.dto.QueryFileDTO
import com.bockerl.snailmember.file.query.dto.QueryFileGatheringDTO
import com.bockerl.snailmember.file.query.mapper.QueryFileConverter
import com.bockerl.snailmember.file.query.repository.FileMapper
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.file.query.vo.response.QueryFileGatheringResponseVO
import com.bockerl.snailmember.file.query.vo.response.QueryFileResponseVO
import org.springframework.stereotype.Service

@Service
class QueryFileServiceImpl(
    private val fileMapper: FileMapper,
    private val fileConverter: QueryFileConverter,
) : QueryFileService {
    override fun readFilesByTarget(queryFileRequestVO: QueryFileRequestVO): List<QueryFileResponseVO> {
        val fileList: List<QueryFileDTO> =
            fileMapper.selectFilesByFileTarget(queryFileRequestVO)
                ?: throw CommonException(ErrorCode.NOT_FOUND_FILE)

        val fileDTOList = fileList.map { file -> fileConverter.dtoToResponseVO(file) }

        return fileDTOList
    }

    override fun readFilesByGatheringId(gatheringId: Long): List<QueryFileGatheringResponseVO> {
        val fileList: List<QueryFileGatheringDTO> =
            fileMapper.selectFilesByGatheringId(gatheringId)
                ?: throw CommonException(ErrorCode.NOT_FOUND_FILE)

        val fileDTOList = fileList.map { file -> fileConverter.gatheringDTOToResponseVO(file) }

        return fileDTOList
    }
}