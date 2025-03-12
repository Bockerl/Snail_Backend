package com.bockerl.snailmember.boardrecomment.query.service

import com.bockerl.snailmember.boardrecomment.query.dto.QueryBoardRecommentDTO
import com.bockerl.snailmember.boardrecomment.query.repository.BoardRecommentMapper
import com.bockerl.snailmember.boardrecomment.query.vo.QueryBoardRecommentResponseVO
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.query.service.QueryFileService
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class QueryBoardRecommentServiceImpl(
    private val boardRecommentMapper: BoardRecommentMapper,
    private val queryMemberService: QueryMemberService,
    private val queryFileService: QueryFileService,
    private val redisTemplate: RedisTemplate<String, Any>,
) : QueryBoardRecommentService {
    @Transactional
    override fun getBoardRecommentByBoardCommentId(
        boardCommentId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardRecommentResponseVO?> {
        // 설명. 캐시 prefix
        val cacheName = "boardRecomments:recomment/$boardCommentId"
        val key = if (lastId != null) "$lastId" + "_" + "$pageSize" else "first"

        redisTemplate.opsForValue().get("$cacheName:$key")?.let {
            redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))
            return it as List<QueryBoardRecommentResponseVO>
        }

        val boardRecommentList: List<QueryBoardRecommentDTO> =
            boardRecommentMapper.selectBoardRecommentsByBoardCommentId(extractDigits(boardCommentId), lastId, pageSize)
        // 설명. member 프로필 이미지를 들고 올 것 및 gif 파일 있는지 판단
        val boardRecommentDTOList =
            boardRecommentList.map { boardRecomment ->
                val responseVO = dtoToResponseVO(boardRecomment)

                if (boardRecomment.boardRecommentContents == null) {
                    val boardRecommentGif =
                        responseVO.boardRecommentId
                            ?.let {
                                QueryFileRequestVO(
                                    fileTargetType = FileTargetType.BOARD_RECOMMENT,
                                    fileTargetId = it,
                                )
                            }?.let {
                                queryFileService.readFilesByTarget(it)
                            }

                    boardRecommentGif?.get(0)?.fileUrl?.let { responseVO.copy(boardRecommentGif = it) }
                } else {
                    responseVO
                }
            }

        redisTemplate.opsForValue().set("$cacheName:$key", boardRecommentDTOList)
        redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))

        return boardRecommentDTOList
    }

    @Transactional
    override fun getBoardRecommentByMemberId(
        memberId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardRecommentResponseVO?> {
        // 설명. 캐시 prefix
        val cacheName = "boardRecomments:member/$memberId"
        val key = if (lastId != null) "$lastId" + "_" + "$pageSize" else "first"

        redisTemplate.opsForValue().get("$cacheName:$key")?.let {
            redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))
            return it as List<QueryBoardRecommentResponseVO>
        }

        val boardRecommentList: List<QueryBoardRecommentDTO> =
            boardRecommentMapper.selectBoardRecommentsByMemberId(extractDigits(memberId), lastId, pageSize)
        // 설명. member 프로필 이미지를 들고 올 것 및 gif 파일 있는지 판단
        val boardRecommentDTOList =
            boardRecommentList.map { boardRecomment ->
                val responseVO = dtoToResponseVO(boardRecomment)

                if (boardRecomment.boardRecommentContents == null) {
                    val boardRecommentGif =
                        responseVO.boardRecommentId
                            ?.let {
                                QueryFileRequestVO(
                                    fileTargetType = FileTargetType.BOARD_RECOMMENT,
                                    fileTargetId = it,
                                )
                            }?.let {
                                queryFileService.readFilesByTarget(it)
                            }

                    boardRecommentGif?.get(0)?.fileUrl?.let { responseVO.copy(boardRecommentGif = it) }
                } else {
                    responseVO
                }
            }

        redisTemplate.opsForValue().set("$cacheName:$key", boardRecommentDTOList)
        redisTemplate.expire("$cacheName:$key", Duration.ofMinutes(5))

        return boardRecommentDTOList
    }

    fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    fun dtoToResponseVO(dto: QueryBoardRecommentDTO): QueryBoardRecommentResponseVO {
        val memberDTO = queryMemberService.selectMemberByMemberId(dto.formatedMemberId)

        return QueryBoardRecommentResponseVO(
            boardRecommentId = dto.formattedId,
            boardCommentId = dto.formattedBoardCommentId,
            boardRecommentContents = dto.boardRecommentContents,
            memberId = dto.formatedMemberId,
            boardId = dto.formattedBoardId,
            active = dto.active,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            memberPhoto = memberDTO.memberPhoto,
            memberNickname = memberDTO.memberNickname,
            boardRecommentGif = "",
        )
    }
}