package com.bockerl.snailmember.boardlike.query.service

import com.bockerl.snailmember.board.query.service.QueryBoardService
import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.boardlike.query.repository.BoardLikeMapper
import com.bockerl.snailmember.boardlike.query.vo.QueryBoardLikeMemberIdsResponseVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class QueryBoardLikeServiceImpl(
    private val boardLikeMapper: BoardLikeMapper,
    private val redisTemplate: RedisTemplate<String, String>,
    private val queryBoardService: QueryBoardService,
    private val queryMemberService: QueryMemberService,
) : QueryBoardLikeService {
    override fun readBoardLike(
        boardId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardLikeMemberIdsResponseVO> {
        val redisKey = "board-like:$boardId"

        // 1. Redis 캐시에서 멤버 ID 목록 조회
        val cachedMemberIds =
            redisTemplate
                .opsForSet()
                .members(redisKey)
                ?.map { it as String }
                ?: emptyList()

        // 2. 캐시의 데이터가 충분하지 않으면 DB에서 조회하고 캐시를 갱신
        val finalMemberIds =
            if (needAdditionalBoard(boardId, cachedMemberIds.size.toLong())) {
                updateCacheWithDbMemberIds(redisKey, boardId, lastId, pageSize)
            } else {
                cachedMemberIds
            }

        // 3. 배치 조회로 멤버 정보 가져오기 (배치 조회 메서드가 있다고 가정)
//        val formattedMemberIds = finalMemberIds.map { it }
//        val members = queryMemberService.selectMembersByMemberIds(finalMemberIds)
        val members = finalMemberIds.map { memberId -> queryMemberService.selectMemberByMemberId(memberId) }

        // 4. ResponseVO 매핑
        return members.map { member ->
            QueryBoardLikeMemberIdsResponseVO(
                memberNickname = member.memberNickname,
                memberId = member.formattedId,
                memberPhoto = member.memberPhoto,
            )
        }
    }

    override fun readBoardIdsByMemberId(
        memberId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardResponseVO> {
        val redisKey = "board-like:$memberId"

        // 1. Redis 캐시에서 게시글 ID 목록 조회
        val cachedBoardIds =
            redisTemplate
                .opsForSet()
                .members(redisKey)
                ?.map { it as String }
                ?: emptyList()

        // 2. 캐시의 데이터가 충분하지 않으면 DB에서 조회하고 캐시를 갱신
        val finalBoardIds =
            if (needAdditionalMember(memberId, cachedBoardIds.size.toLong())) {
                updateCacheWithDbBoardIds(redisKey, memberId, lastId, pageSize)
            } else {
                cachedBoardIds
            }

        val boards = finalBoardIds.map { boardId -> queryBoardService.readBoardByBoardId(memberId) }

        return boards
    }

    override fun readBoardLikeCount(boardId: String): Long {
        val redisKey = "board-like:count:$boardId"
        val countStr = redisTemplate.opsForValue().get(redisKey)
        return countStr?.toLongOrNull() ?: 0L
    }

    private fun needAdditionalBoard(
        boardId: String,
        redisDataSize: Long,
    ): Boolean {
        val totalLikeCount = readDBLikeCountByBoardId(boardId)
        val threshold = totalLikeCount * 0.9

        return redisDataSize <= threshold
    }

    private fun needAdditionalMember(
        memberId: String,
        redisDataSize: Long,
    ): Boolean {
        val totalLikeCount = readDBLikeCountByMemberId(memberId)
        val threshold = totalLikeCount * 0.9

        return redisDataSize <= threshold
    }

    /**
     * DB에서 멤버 ID 목록을 조회하고, Redis 캐시를 갱신하는 메서드.
     */
    private fun updateCacheWithDbMemberIds(
        redisKey: String,
        boardId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<String> {
        // DB에서 boardLikeMapper를 통해 멤버 ID 조회
        val dbMemberIds =
            boardLikeMapper
                .selectMemberIdsByBoardId(extractDigits(boardId), lastId, pageSize)
                .map { formattedMemberId(it.memberId) }

        // Redis 캐시 업데이트: 기존 캐시를 삭제 후 DB 결과를 저장
        if (dbMemberIds.isNotEmpty()) {
            redisTemplate.delete(redisKey)
            redisTemplate.opsForSet().add(redisKey, *dbMemberIds.toTypedArray())
            redisTemplate.expire(redisKey, Duration.ofDays(1))
        }
        return dbMemberIds
    }

    /**
     * DB에서 멤버 ID 목록을 조회하고, Redis 캐시를 갱신하는 메서드.
     */
    private fun updateCacheWithDbBoardIds(
        redisKey: String,
        memberId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<String> {
        // DB에서 boardLikeMapper를 통해 멤버 ID 조회
        val dbMemberIds =
            boardLikeMapper
                .selectMemberIdsByBoardId(extractDigits(memberId), lastId, pageSize)
                .map { formattedBoardId(it.boardId) }

        // Redis 캐시 업데이트: 기존 캐시를 삭제 후 DB 결과를 저장
        if (dbMemberIds.isNotEmpty()) {
            redisTemplate.delete(redisKey)
            redisTemplate.opsForSet().add(redisKey, *dbMemberIds.toTypedArray())
            redisTemplate.expire(redisKey, Duration.ofDays(1))
        }
        return dbMemberIds
    }

    private fun readDBLikeCountByMemberId(memberId: String): Long = boardLikeMapper.selectCountByMemberId(extractDigits(memberId))

    private fun readDBLikeCountByBoardId(boardId: String): Long = boardLikeMapper.selectCountByBoardId(extractDigits(boardId))

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    private fun formattedMemberId(memberId: Long): String = "MEM-${memberId.toString().padStart(8, '0') ?: "00000000"}"

    private fun formattedBoardId(boardId: Long): String = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"
}