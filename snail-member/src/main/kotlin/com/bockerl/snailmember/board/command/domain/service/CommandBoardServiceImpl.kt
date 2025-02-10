package com.bockerl.snailmember.board.command.domain.service

import com.bockerl.snailmember.board.command.application.service.CommandBoardService
import com.bockerl.snailmember.board.command.domain.aggregate.entity.Board
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardRequestVO
import com.bockerl.snailmember.board.command.domain.aggregate.vo.request.CommandBoardUpdateRequestVO
import com.bockerl.snailmember.board.command.domain.repository.CommandBoardRepository
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.command.domain.aggregate.vo.CommandFileRequestVO
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CommandBoardServiceImpl(
    private val commandBoardRepository: CommandBoardRepository,
    private val commandFileService: CommandFileService,
)  :CommandBoardService{

    @Transactional
    override fun createBoard(commandBoardRequestVO: CommandBoardRequestVO, files: List<MultipartFile>) {

        val board = Board(
            boardContents = commandBoardRequestVO.boardContents,
            boardType = commandBoardRequestVO.boardType,
            boardTag = commandBoardRequestVO.boardTag,
            boardLocation = commandBoardRequestVO.boardLocation,
            boardAccessLevel = commandBoardRequestVO.boardAccessLevel,
            memberId = commandBoardRequestVO.memberId,
        )


        val boardEntity = commandBoardRepository.save(board)

        if(files.isNotEmpty()) {
            val commandFileRequestVO = boardEntity.boardId?.let {
                CommandFileRequestVO(
                    fileTargetType = FileTargetType.BOARD,
                    fileTargetId = it,
                    memberId = commandBoardRequestVO.memberId,
                )
            }

            if (commandFileRequestVO != null) {
                commandFileService.uploadFiles(files, commandFileRequestVO)
            };
        }
    }

    override fun updateBoard(commandBoardUpdateRequestVO: CommandBoardUpdateRequestVO, files: List<MultipartFile>) {

        val boardId = commandBoardUpdateRequestVO.boardId

//        val parsingBoardId = boardId.
//
//        val board = commandBoardRepository.find

//        val board = Board(
//            boardContents = commandBoardUpdateRequestVO.boardContents,
//            boardType = commandBoardUpdateRequestVO.boardType,
//            boardTag = commandBoardUpdateRequestVO.boardTag,
//            boardLocation = commandBoardUpdateRequestVO.boardLocation,
//            boardAccessLevel = commandBoardUpdateRequestVO.boardAccessLevel,
//            memberId = commandBoardUpdateRequestVO.memberId,
//        )

//        val boardEntity = commandBoardRepository.save(board)
//
//        if(files.isNotEmpty()) {
//            val commandFileRequestVO = boardEntity.boardId?.let {
//                CommandFileRequestVO(
//                    fileTargetType = FileTargetType.BOARD,
//                    fileTargetId = it,
//                    memberId = commandBoardRequestVO.memberId,
//                )
//            }
//
//            if (commandFileRequestVO != null) {
//                commandFileService.uploadFiles(files, commandFileRequestVO)
//            };
//        }
    }

    fun extractDigits(input: String): Int {
        return input.filter { it.isDigit() }.toInt()
    }
}