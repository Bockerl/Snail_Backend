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
    private val redisTemplate: RedisTemplate<String, Any>,
    private val queryBoardService: QueryBoardService,
    private val queryMemberService: QueryMemberService,
) : QueryBoardLikeService {
    override fun readBoardLike(boardId: String): List<QueryBoardLikeMemberIdsResponseVO> {
        val recentLikes =
            redisTemplate.opsForSet().members("board-like:$boardId")?.map { it as String } ?: emptyList()
        // 설명. redis에 데이터가 충분하지 않을 경우 mongodb에서 추가 조회
        return if (needAdditionalBoard(boardId, recentLikes.size.toLong())) {
            // 설명. memberId 명단 뽑기
            val dbMemberIds = boardLikeMapper.selectMemberIdsByBoardId(extractDigits(boardId)).map { it.memberId }
            // 설명. 캐시에 저장 하기
            redisTemplate.opsForSet().add("board-like:$boardId", *dbMemberIds.toTypedArray())
            redisTemplate.expire("board-like:$boardId", Duration.ofDays(1))
            // 설명. memeberId를 통해서 member 정보들 list 뽑기
            val members = dbMemberIds.map { memberId -> queryMemberService.selectMemberByMemberId(formattedMemberId(memberId)) }
//            val members = (dbMemberIds + recentLikes).map { queryMemberService.selectMemberByMemberId(it) }
            // 설명. ResponseVO에 담아주기(memebers list의 각 인덱스의 memberNickname, memberId, memberProfile를 넣은 vo list
            members.map { member ->
                QueryBoardLikeMemberIdsResponseVO(
                    memberNickname = member.memberNickName,
                    memberId = member.memberId,
                    memberPhoto = member.memberPhoto,
                )
            }
        } else {
            // 설명. redis에서의 데이터도 충분한 경우
            val members = recentLikes.map { queryMemberService.selectMemberByMemberId(it) }

            members.map { member ->
                QueryBoardLikeMemberIdsResponseVO(
                    memberNickname = member.memberNickName,
                    memberId = member.memberId,
                    memberPhoto = member.memberPhoto,
                )
            }
        }
    }

    override fun readBoardIdsByMemberId(memberId: String): List<QueryBoardResponseVO> {
        // 설명. 유저가 좋아요한 게시글들 모아보는 함수
        val recentBoardIds =
            redisTemplate.opsForSet().members("board-like:$memberId")?.map { it as String } ?: emptyList()

        return if (needAdditionalMember(memberId, recentBoardIds.size.toLong())) {
            // 설명. 둘 다 조회해야 할 때
            val dbBoardIds = boardLikeMapper.selectBoardIdsByMemberId(extractDigits(memberId)).map { it.boardId }

            redisTemplate.opsForSet().add("board-like:$memberId", *dbBoardIds.toTypedArray())
            redisTemplate.expire("board-like:$memberId", Duration.ofDays(1))

            // 설명. 어떤 전략을 쓸 지 정해야 함
            dbBoardIds.map { boardId -> queryBoardService.readBoardByBoardId(formattedBoardId(boardId)) }
        } else {
            recentBoardIds.map { queryBoardService.readBoardByBoardId(it) }
        }
    }

    override fun readBoardLikeCount(boardId: String): Long {
        TODO("redis에서 관리하고 읽어올 예정입니다.")
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

    private fun readDBLikeCountByMemberId(memberId: String): Long = boardLikeMapper.selectCountByMemberId(extractDigits(memberId))

    private fun readDBLikeCountByBoardId(boardId: String): Long = boardLikeMapper.selectCountByBoardId(extractDigits(boardId))

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    fun formattedMemberId(memberId: Long): String = "MEM-${memberId.toString().padStart(8, '0') ?: "00000000"}"

    fun formattedBoardId(boardId: Long): String = "BOA-${boardId.toString().padStart(8, '0') ?: "00000000"}"
}