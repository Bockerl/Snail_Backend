package com.bockerl.snailmember.boardcomment.query.service

import com.bockerl.snailmember.boardcomment.query.dto.QueryBoardCommentDTO
import com.bockerl.snailmember.boardcomment.query.repository.BoardCommentMapper
import com.bockerl.snailmember.boardcomment.query.vo.QueryBoardCommentResponseVO
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.query.service.QueryFileService
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class QueryBoardCommentServiceImpl(
    private val boardCommentMapper: BoardCommentMapper,
    private val queryMemberService: QueryMemberService,
    private val queryFileService: QueryFileService,
) : QueryBoardCommentService {
    @Transactional
    @Cacheable(value = ["boardComments"], key = "#lastId != null ? #lastId + '_' + #pageSize : 'first'")
    override fun getBoardCommentByBoardId(
        boardId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardCommentResponseVO?> {
        val boardCommentList: List<QueryBoardCommentDTO> =
            boardCommentMapper.selectBoardCommentsByBoardId(extractDigits(boardId), lastId, pageSize)
        // 설명. member 프로필 이미지를 들고 올 것 및 gif 파일 있는지 판단
        val boardCommentDTOList =
            boardCommentList.map { boardComment ->
                val responseVO = dtoToResponseVO(boardComment)

                if (boardComment.boardCommentContents == null) {
                    val boardCommentGif =
                        responseVO.boardCommentId
                            ?.let {
                                QueryFileRequestVO(
                                    fileTargetType = FileTargetType.BOARD_COMMENT,
                                    fileTargetId = it,
                                )
                            }?.let {
                                queryFileService.readFilesByTarget(it)
                            }

                    boardCommentGif?.get(0)?.fileUrl?.let { responseVO.copy(boardCommentGif = it) }
                } else {
                    responseVO
                }
            }
        return boardCommentDTOList
    }

    // 설명. 내 댓글 목록 추가 예정

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    fun dtoToResponseVO(dto: QueryBoardCommentDTO): QueryBoardCommentResponseVO {
        val memberDTO = queryMemberService.selectMemberByMemberId(dto.formatedMemberId)

        return QueryBoardCommentResponseVO(
            boardCommentId = dto.formattedId,
            boardCommentContents = dto.boardCommentContents,
            memberId = dto.formatedMemberId,
            boardId = dto.formattedBoardId,
            active = dto.active,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            memberPhoto = memberDTO.memberPhoto,
            memberNickname = memberDTO.memberNickName,
            boardCommentGif = "",
        )
    }
}