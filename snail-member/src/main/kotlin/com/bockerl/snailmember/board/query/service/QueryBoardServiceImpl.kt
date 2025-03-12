/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.query.dto.QueryBoardDTO
import com.bockerl.snailmember.board.query.repository.BoardMapper
import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.query.service.QueryFileService
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class QueryBoardServiceImpl(
    private val boardMapper: BoardMapper,
    private val queryMemberService: QueryMemberService,
    private val queryFileService: QueryFileService,
    private val redisTemplate: RedisTemplate<String, Any>,
) : QueryBoardService {
    @Transactional
    override fun readBoardByBoardId(boardId: String): QueryBoardResponseVO {
        val parsingBoardId = extractDigits(boardId)

        val board =
            boardMapper.selectBoardByBoardId(parsingBoardId)
                ?: throw CommonException(ErrorCode.NOT_FOUND_BOARD)

        return dtoToResponseVO(board)
    }

    @Transactional
    override fun readBoardByBoardType(
        boardType: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardResponseVO> {
        // 설명. 캐시 prefix
        val cacheName = "board/$boardType"
        val key = if (lastId != null) "$lastId" + "_" + "$pageSize" else "first"

        redisTemplate.opsForValue().get("$cacheName:$key")?.let {
            redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))
            return it as List<QueryBoardResponseVO>
        }
        // 설명. List collection(immutable)
        val boardList: List<QueryBoardDTO> =
            boardMapper.selectBoardByBoardType(boardType, lastId, pageSize)
                ?: throw CommonException(ErrorCode.NOT_FOUND_BOARD)

        // 설명. mapping 해주기 및 각 게시글의 파일 리스트들 들고 오기
        val boardDTOList =
            boardList.map { board ->
                val fileList =
                    queryFileService.readFilesByTarget(
                        QueryFileRequestVO(FileTargetType.BOARD, formattedBoardId(board.boardId)),
                    )
                val fileUrls = fileList.map { it.fileUrl }

                val responseVO = dtoToResponseVO(board)
                responseVO.copy(fileList = fileUrls)
            }

        redisTemplate.opsForValue().set("$cacheName:$key", boardDTOList)
        redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))

        return boardDTOList
    }

    @Transactional
    override fun readBoardByBoardTag(
        boardTagList: List<String>,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardResponseVO> {
        // 설명. 캐시 prefix
        // 설명. 전체 태그 조합 정렬 및 '_'로 연결
        val sortedTags = boardTagList.sorted().joinToString("_")
        val cacheName = "board/$sortedTags"
        val key = if (lastId != null) "$lastId" + "_" + "$pageSize" else "first"

        redisTemplate.opsForValue().get("$cacheName:$key")?.let {
            redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))
            return it as List<QueryBoardResponseVO>
        }

        val boardList: List<QueryBoardDTO> =
            boardMapper.selectBoardByBoardTag(boardTagList, lastId, pageSize)
                ?: throw CommonException(ErrorCode.NOT_FOUND_BOARD)

        val boardDTOList =
            boardList.map { board ->
                val fileList =
                    queryFileService.readFilesByTarget(
                        QueryFileRequestVO(FileTargetType.BOARD, formattedBoardId(board.boardId)),
                    )
                val fileUrls = fileList.map { it.fileUrl }

                val responseVO = dtoToResponseVO(board)
                responseVO.copy(fileList = fileUrls)
            }

        redisTemplate.opsForValue().set("$cacheName:$key", boardDTOList)
        redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))

        return boardDTOList
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    private fun formattedBoardId(boardId: Long): String = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"

    private fun dtoToResponseVO(dto: QueryBoardDTO): QueryBoardResponseVO {
        val memberDTO = queryMemberService.selectMemberByMemberId(dto.formatedMemberId)

        return QueryBoardResponseVO(
            boardId = dto.formattedId,
            boardContents = dto.boardContents,
            queryBoardType = dto.boardType,
            queryBoardTag = dto.boardTag,
            boardLocation = dto.boardLocation,
            boardAccessLevel = dto.boardAccessLevel,
            boardView = dto.boardView,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            active = dto.active,
            memberPhoto = memberDTO.memberPhoto,
            memberNickname = memberDTO.memberNickname,
            memberId = dto.formatedMemberId,
        )
    }
}