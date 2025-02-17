/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.file.query.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.query.dto.QueryFileDTO
import com.bockerl.snailmember.file.query.dto.QueryFileGatheringDTO
import com.bockerl.snailmember.file.query.dto.QueryFileRequestDTO
import com.bockerl.snailmember.file.query.repository.FileMapper
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.file.query.vo.response.QueryFileGatheringResponseVO
import com.bockerl.snailmember.file.query.vo.response.QueryFileResponseVO
import org.springframework.stereotype.Service

@Service
class QueryFileServiceImpl(
    private val fileMapper: FileMapper,
) : QueryFileService {
    override fun readFilesByTarget(queryFileRequestVO: QueryFileRequestVO): List<QueryFileResponseVO> {
        val requestDTO =
            QueryFileRequestDTO(
                fileTargetType = queryFileRequestVO.fileTargetType,
                fileTargetId = extractDigits(queryFileRequestVO.fileTargetId),
            )

        val fileList: List<QueryFileDTO> =
            fileMapper.selectFilesByFileTarget(requestDTO)
                ?: throw CommonException(ErrorCode.NOT_FOUND_FILE)

        val fileDTOList = fileList.map { file -> dtoToResponseVO(file) }

        return fileDTOList
    }

    override fun readFilesByGatheringId(gatheringId: String): List<QueryFileGatheringResponseVO> {
        val fileList: List<QueryFileGatheringDTO> =
            fileMapper.selectFilesByGatheringId(extractDigits(gatheringId))
                ?: throw CommonException(ErrorCode.NOT_FOUND_FILE)

        val fileDTOList = fileList.map { file -> gatheringDTOToResponseVO(file) }

        return fileDTOList
    }

    private fun dtoToResponseVO(dto: QueryFileDTO): QueryFileResponseVO =
        QueryFileResponseVO(
            fileId = dto.formattedId,
            fileName = dto.fileName,
            fileType = dto.fileType,
            fileUrl = dto.fileUrl,
            active = dto.active,
            memberId = dto.formattedMemberId,
            fileTargetType = dto.fileTargetType,
            fileTargetId = dto.formattedFileTargetId,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
        )

    private fun gatheringDTOToResponseVO(dto: QueryFileGatheringDTO): QueryFileGatheringResponseVO =
        QueryFileGatheringResponseVO(
            fileId = dto.formattedId,
            fileName = dto.fileName,
            fileType = dto.fileType,
            fileUrl = dto.fileUrl,
            active = dto.active,
            memberId = dto.formattedMemberId,
            fileTargetType = dto.fileTargetType,
            fileTargetId = dto.formattedFileTargetId,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            gatheringId = dto.formattedGatheringId,
        )

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}