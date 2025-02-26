/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.query.dto.QueryBoardDTO
import com.bockerl.snailmember.board.query.mapper.QueryBoardConverter
import com.bockerl.snailmember.board.query.repository.BoardMapper
import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

// 설명. :바로 다음에 인터페이스 타입이 오는 것 ??
@Service
class QueryBoardServiceImpl(
    private val boardMapper: BoardMapper,
    private val boardConverter: QueryBoardConverter,
) : QueryBoardService {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun readBoardByBoardId(boardId: String): QueryBoardResponseVO {
        val parsingBoardId = extractDigits(boardId)

        val board =
            boardMapper.selectBoardByBoardId(parsingBoardId)
                ?: throw CommonException(ErrorCode.NOT_FOUND_BOARD)

        return boardConverter.dtoToResponseVO(board)
    }

    override fun readBoardByBoardType(boardType: String): List<QueryBoardResponseVO> {
        // 설명. List collection(immutable)
        val boardList: List<QueryBoardDTO> =
            boardMapper.selectBoardByBoardType(boardType)
                ?: throw CommonException(ErrorCode.NOT_FOUND_BOARD)

        // 설명. mapping 해주기
        val boardDTOList = boardList.map { boardDTO -> boardConverter.dtoToResponseVO(boardDTO) }

        return boardDTOList
    }

    override fun readBoardByBoardTag(boardTagList: List<String>): List<QueryBoardResponseVO> {
        val boardList: List<QueryBoardDTO> =
            boardMapper.selectBoardByBoardTag(boardTagList)
                ?: throw CommonException(ErrorCode.NOT_FOUND_BOARD)

        val boardDTOList = boardList.map { boardDTO -> boardConverter.dtoToResponseVO(boardDTO) }

        return boardDTOList
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}