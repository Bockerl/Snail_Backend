package com.bockerl.snailmember.boardcomment.query.service

import com.bockerl.snailmember.boardcomment.query.dto.QueryBoardCommentDTO
import com.bockerl.snailmember.boardcomment.query.repository.BoardCommentMapper
import com.bockerl.snailmember.boardcomment.query.vo.QueryBoardCommentResponseVO
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.query.service.QueryFileService
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class QueryBoardCommentServiceImpl(
    private val boardCommentMapper: BoardCommentMapper,
    private val queryMemberService: QueryMemberService,
    private val queryFileService: QueryFileService,
    private val redisTemplate: RedisTemplate<String, Any>,
) : QueryBoardCommentService {
    @Transactional
    override fun getBoardCommentByBoardId(
        boardId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardCommentResponseVO?> {
        // 설명. 캐시 prefix
        val cacheName = "boardComments:comment/$boardId"
        val key = if (lastId != null) "$lastId" + "_" + "$pageSize" else "first"

        redisTemplate.opsForValue().get("$cacheName:$key")?.let {
            redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))
            return it as List<QueryBoardCommentResponseVO>
        }

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

        redisTemplate.opsForValue().set("$cacheName:$key", boardCommentDTOList)
        redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))

        return boardCommentDTOList
    }

    @Transactional
    override fun getBoardCommentByMemberId(
        memberId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardCommentResponseVO?> {
        // 설명. 캐시 prefix
        val cacheName = "boardComments:member/$memberId"
        val key = if (lastId != null) "$lastId" + "_" + "$pageSize" else "first"

        redisTemplate.opsForValue().get("$cacheName:$key")?.let {
            redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))
            return it as List<QueryBoardCommentResponseVO>
        }

        val boardCommentList: List<QueryBoardCommentDTO> =
            boardCommentMapper.selectBoardCommentsByMemberId(extractDigits(memberId), lastId, pageSize)
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

        redisTemplate.opsForValue().set("$cacheName:$key", boardCommentDTOList)
        redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))

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
            memberNickname = memberDTO.memberNickname,
            boardCommentGif = "",
        )
    }
}