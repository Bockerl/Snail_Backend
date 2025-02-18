package com.bockerl.snailmember.boardcomment.query.service

import com.bockerl.snailmember.boardcomment.query.dto.QueryBoardCommentDTO
import com.bockerl.snailmember.boardcomment.query.repository.BoardCommentMapper
import com.bockerl.snailmember.boardcomment.query.vo.QueryBoardCommentResponseVO
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.query.service.QueryFileService
import com.bockerl.snailmember.file.query.vo.request.QueryFileRequestVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import jakarta.transaction.Transactional
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.stereotype.Service

@Service
class QueryBoardCommentServiceImpl(
    private val boardCommentMapper: BoardCommentMapper,
    private val queryMemberService: QueryMemberService,
    private val queryFileService: QueryFileService,
    private val cacheManager: RedisCacheManager,
) : QueryBoardCommentService {
    @Transactional
    override fun getBoardCommentByBoardId(
        boardId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardCommentResponseVO?> {
        // 설명. 캐시 prefix
        val cacheName = "boardComments/$boardId"
        // 설명. 해당 캐시 이름이 없을 떄떄 캐시 매니저에 새 캐시 등록
        val cache = cacheManager.getCache(cacheName)
        val key = if (lastId != null) "$lastId" + "_" + "$pageSize" else "first"

        // 설명. cache.get(key)는 Cache.ValueWrapper이다. 따라서 .get() 메소드를 호출해서 실제 값을 꺼내야함
        cache?.get(key)?.get()?.let {
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

        cache?.put(key, boardCommentDTOList)

        return boardCommentDTOList
    }

    @Transactional
    override fun getBoardCommentByMemberId(
        memberId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardCommentResponseVO?> {
        // 설명. 캐시 prefix
        val cacheName = "boardComments/$memberId"
        // 설명. 해당 캐시 이름이 없을 떄떄 캐시 매니저에 새 캐시 등록
        val cache = cacheManager.getCache(cacheName)
        val key = if (lastId != null) "$lastId" + "_" + "$pageSize" else "first"

        // 설명. cache.get(key)는 Cache.ValueWrapper이다. 따라서 .get() 메소드를 호출해서 실제 값을 꺼내야함
        cache?.get(key)?.get()?.let {
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

        cache?.put(key, boardCommentDTOList)

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