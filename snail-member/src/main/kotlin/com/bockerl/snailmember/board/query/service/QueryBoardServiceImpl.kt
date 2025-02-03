package com.bockerl.snailmember.board.query.service

import com.bockerl.snailmember.board.command.application.dto.BoardDTO
import com.bockerl.snailmember.board.command.application.mapper.BoardConverter
import com.bockerl.snailmember.board.query.repository.BoardMapper
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
/* 설명. :바로 다음에 인터페이스 타입이 오는 것 ?? */
class QueryBoardServiceImpl(
    private val boardMapper: BoardMapper,
    private val boardConverter: BoardConverter,
) :QueryBoardService {
    override fun readBoardByBoardId(boardId: Long): BoardDTO {
        val board =
            boardMapper.selectBoardByBoardId(boardId)
                ?: throw CommonException(ErrorCode.NOT_FOUND_BOARD)
        return boardConverter.entityToDTO(board)
    }
}