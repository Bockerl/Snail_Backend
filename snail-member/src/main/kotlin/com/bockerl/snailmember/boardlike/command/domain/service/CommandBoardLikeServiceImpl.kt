package com.bockerl.snailmember.boardlike.command.domain.service

import com.bockerl.snailmember.board.query.service.QueryBoardService
import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.boardlike.command.application.service.CommandBoardLikeService
import com.bockerl.snailmember.boardlike.command.domain.aggregate.enum.BoardLikeActionType
import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import com.bockerl.snailmember.boardlike.command.domain.repository.BoardLikeRepository
import com.bockerl.snailmember.boardlike.command.domain.vo.request.CommandBoardLikeRequestVO
import com.bockerl.snailmember.boardlike.command.domain.vo.response.CommandBoardLikeMemberIdsResponseVO
import com.bockerl.snailmember.member.query.service.QueryMemberService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CommandBoardLikeServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val boardLikeRepository: BoardLikeRepository,
    private val queryMemberService: QueryMemberService,
    private val queryBoardService: QueryBoardService,
    private val kafkaBoardLikeTemplate: KafkaTemplate<String, BoardLikeEvent>,
) : CommandBoardLikeService {
    override fun createBoardLike(commandBoardLikeRequestVO: CommandBoardLikeRequestVO) {
        // 설명. redis에서 board pk 기준 인덱스 설정 및 member pk 기준 인덱스 설정 할 것
        // 설명. 집합으로 각 인덱스 관리. cold data 분리를 위해 expire 설정(1일) -> 호출될 때 마다 갱신됨

        // 설명. 1. board pk 기준 인덱스
        redisTemplate
            .opsForSet()
            .add("board-like:${commandBoardLikeRequestVO.boardId}", commandBoardLikeRequestVO.memberId)
        redisTemplate.expire("board-like:${commandBoardLikeRequestVO.boardId}", Duration.ofDays(1))
        // 설명. 2. member pk 기준 인덱스 (역 인덱스)
        redisTemplate
            .opsForSet()
            .add("board-like:${commandBoardLikeRequestVO.memberId}", commandBoardLikeRequestVO.boardId)
        redisTemplate.expire("board-like:${commandBoardLikeRequestVO.memberId}", Duration.ofDays(1))

        // Kafka에 이벤트 발행
        val event =
            BoardLikeEvent(
                boardId = commandBoardLikeRequestVO.boardId,
                memberId = commandBoardLikeRequestVO.memberId,
                boardLikeActionType = BoardLikeActionType.LIKE,
            )

//        kafkaTemplate.send("board-like-events", event)
        kafkaBoardLikeTemplate.send("board-like-events", event)
    }

    override fun deleteBoardLike(commandBoardLikeRequestVO: CommandBoardLikeRequestVO) {
        // 설명. 1. board pk 기준 인덱스
        redisTemplate
            .opsForSet()
            .remove("board-like:${commandBoardLikeRequestVO.boardId}", commandBoardLikeRequestVO.memberId)

        // 설명. 2. member pk 기준 인덱스 (역 인덱스)
        redisTemplate
            .opsForSet()
            .remove("board-like:${commandBoardLikeRequestVO.memberId}", commandBoardLikeRequestVO.boardId)

        // Kafka에 이벤트 발행
        val event =
            BoardLikeEvent(
                boardId = commandBoardLikeRequestVO.boardId,
                memberId = commandBoardLikeRequestVO.memberId,
                boardLikeActionType = BoardLikeActionType.UNLIKE,
            )

        kafkaBoardLikeTemplate.send("board-like-events", event)
    }

    override fun readBoardLike(boardId: String): List<CommandBoardLikeMemberIdsResponseVO> {
        val recentLikes =
            redisTemplate.opsForSet().members("board-like:$boardId")?.map { it as String } ?: emptyList()
        // 설명. redis에 데이터가 충분하지 않을 경우 mongodb에서 추가 조회
        return if (needAdditionalBoard(boardId, recentLikes.size.toLong())) {
            // 설명. memberId 명단 뽑기
            val dbMemberIds = boardLikeRepository.findMemberIdsByBoardId(boardId).map { it.memberId }
            // 설명. 캐시에 저장 하기
            redisTemplate.opsForSet().add("board-like:$boardId", *dbMemberIds.toTypedArray())
            redisTemplate.expire("board-like:$boardId", Duration.ofDays(1))
            // 설명. memeberId를 통해서 member 정보들 list 뽑기
            val members = (dbMemberIds + recentLikes).map { queryMemberService.selectMemberByMemberId(it) }
            // 설명. ResponseVO에 담아주기(memebers list의 각 인덱스의 memberNickname, memberId, memberProfile를 넣은 vo list
            members.map { member ->
                CommandBoardLikeMemberIdsResponseVO(
                    memberNickname = member.memberNickName,
                    memberId = member.memberId,
                    memberPhoto = member.memberPhoto,
                )
            }
        } else {
            // 설명. redis에서의 데이터도 충분한 경우
            val members = recentLikes.map { queryMemberService.selectMemberByMemberId(it) }

            members.map { member ->
                CommandBoardLikeMemberIdsResponseVO(
                    memberNickname = member.memberNickName,
                    memberId = member.memberId,
                    memberPhoto = member.memberPhoto,
                )
            }
        }
    }

    override fun readBoardLikeCount(boardId: String): Long {
        // 설명. 스크롤 할 때 보일 총 좋아요 수..
        val redisCount = redisTemplate.opsForSet().size("board-like:$boardId") ?: 0

        return if (needAdditionalBoard(boardId, redisCount)) {
            redisCount + readDBLikeCountByBoardId(boardId)
        } else {
            redisCount
        }
    }

    override fun readBoardIdsByMemberId(memberId: String): List<QueryBoardResponseVO> {
        // 설명. 유저가 좋아요한 게시글들 모아보는 함수
        val recentBoardIds =
            redisTemplate.opsForSet().members("board-like:$memberId")?.map { it as String } ?: emptyList()

        return if (needAdditionalMember(memberId, recentBoardIds.size.toLong())) {
            // 설명. 둘 다 조회해야 할 때
            val dbBoardIds = boardLikeRepository.findBoardIdsByMemberId(memberId).map { it.boardId }

            redisTemplate.opsForSet().add("board-like:$memberId", *dbBoardIds.toTypedArray())
            redisTemplate.expire("board-like:$memberId", Duration.ofDays(1))

            (dbBoardIds + recentBoardIds).map { queryBoardService.readBoardByBoardId(it) }
        } else {
            recentBoardIds.map { queryBoardService.readBoardByBoardId(it) }
        }
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

    private fun readDBLikeCountByMemberId(memberId: String): Long = boardLikeRepository.countByMemberId(memberId)

    private fun readDBLikeCountByBoardId(boardId: String): Long = boardLikeRepository.countByBoardId(boardId)
}