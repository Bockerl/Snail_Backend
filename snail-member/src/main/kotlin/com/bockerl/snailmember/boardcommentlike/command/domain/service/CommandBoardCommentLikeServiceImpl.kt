package com.bockerl.snailmember.boardcommentlike.command.domain.service

import com.bockerl.snailmember.board.query.service.QueryBoardService
import com.bockerl.snailmember.boardcommentlike.command.application.dto.CommandBoardCommentLikeDTO
import com.bockerl.snailmember.boardcommentlike.command.application.service.CommandBoardCommentLikeService
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity.BoardCommentLike
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.enum.BoardCommentLikeActionType
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event.BoardCommentLikeEvent
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.vo.response.CommandBoardCommentLikeMemberIdsResponseVO
import com.bockerl.snailmember.boardcommentlike.command.domain.repository.BoardCommentLikeRepository
import com.bockerl.snailmember.member.query.service.QueryMemberService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CommandBoardCommentLikeServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val boardCommentLikeRepository: BoardCommentLikeRepository,
    private val queryMemberService: QueryMemberService,
    private val queryBoardService: QueryBoardService,
    private val kafkaBoardCommentLikeTemplate: KafkaTemplate<String, BoardCommentLikeEvent>,
) : CommandBoardCommentLikeService {
    override fun createBoardCommentLike(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO) {
        // 설명. redis에서 board pk 기준 인덱스 설정 및 member pk 기준 인덱스 설정 할 것
        // 설명. 집합으로 각 인덱스 관리. cold data 분리를 위해 expire 설정(1일) -> 호출될 때 마다 갱신됨

        // 설명. 1. board-comment pk 기준 인덱스
        redisTemplate
            .opsForSet()
            .add("board-comment-like:${commandBoardCommentLikeDTO.boardCommentId}", commandBoardCommentLikeDTO.memberId)
        redisTemplate.expire("board-comment-like:${commandBoardCommentLikeDTO.boardCommentId}", Duration.ofDays(1))
        // 설명. 2. member pk 기준 인덱스 (역 인덱스)
        redisTemplate
            .opsForSet()
            .add("board-comment-like:${commandBoardCommentLikeDTO.memberId}", commandBoardCommentLikeDTO.boardCommentId)
        redisTemplate.expire("board-comment-like:${commandBoardCommentLikeDTO.memberId}", Duration.ofDays(1))

        // Kafka에 이벤트 발행(게시글 pk 포함해서 보내주기)
        val event =
            BoardCommentLikeEvent(
                boardId = commandBoardCommentLikeDTO.boardId,
                memberId = commandBoardCommentLikeDTO.memberId,
                boardCommentId = commandBoardCommentLikeDTO.boardCommentId,
                boardCommentLikeActionType = BoardCommentLikeActionType.LIKE,
            )

//        kafkaTemplate.send("board-like-events", event)
        kafkaBoardCommentLikeTemplate.send("board-comment-like-events", event)
    }

    override fun createBoardCommentLikeEventList(boardCommentLikeList: List<BoardCommentLike>) {
        boardCommentLikeRepository.saveAll(boardCommentLikeList)
    }

    override fun deleteBoardCommentLike(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO) {
        // 설명. 1. board pk 기준 인덱스
        redisTemplate
            .opsForSet()
            .remove("board-comment-like:${commandBoardCommentLikeDTO.boardCommentId}", commandBoardCommentLikeDTO.memberId)

        // 설명. 2. member pk 기준 인덱스 (역 인덱스)
        redisTemplate
            .opsForSet()
            .remove("board-comment-like:${commandBoardCommentLikeDTO.memberId}", commandBoardCommentLikeDTO.boardCommentId)

        // Kafka에 이벤트 발행
        val event =
            BoardCommentLikeEvent(
                boardId = commandBoardCommentLikeDTO.boardId,
                memberId = commandBoardCommentLikeDTO.memberId,
                boardCommentId = commandBoardCommentLikeDTO.boardCommentId,
                boardCommentLikeActionType = BoardCommentLikeActionType.UNLIKE,
            )

        kafkaBoardCommentLikeTemplate.send("board-comment-like-events", event)
    }

    override fun deleteBoardCommentLikeEvent(boardCommentLike: BoardCommentLike) {
        boardCommentLikeRepository.deleteByMemberIdAndBoardCommentId(boardCommentLike.memberId, boardCommentLike.boardCommentId)
    }

    override fun readBoardCommentLike(boardCommentId: String): List<CommandBoardCommentLikeMemberIdsResponseVO> {
        val recentLikes =
            redisTemplate.opsForSet().members("board-comment-like:$boardCommentId")?.map { it as String } ?: emptyList()
        // 설명. redis에 데이터가 충분하지 않을 경우 mongodb에서 추가 조회
        return if (needAdditionalBoard(boardCommentId, recentLikes.size.toLong())) {
            // 설명. memberId 명단 뽑기
            val dbMemberIds = boardCommentLikeRepository.findMemberIdsByBoardId(boardCommentId).map { it.memberId }
            // 설명. 캐시에 저장 하기
            redisTemplate.opsForSet().add("board-commet-like:$boardCommentId", *dbMemberIds.toTypedArray())
            redisTemplate.expire("board-comment-like:$boardCommentId", Duration.ofDays(1))
            // 설명. memeberId를 통해서 member 정보들 list 뽑기
            val members = (dbMemberIds + recentLikes).map { queryMemberService.selectMemberByMemberId(it) }
            // 설명. ResponseVO에 담아주기(memebers list의 각 인덱스의 memberNickname, memberId, memberProfile를 넣은 vo list
            members.map { member ->
                CommandBoardCommentLikeMemberIdsResponseVO(
                    memberNickname = member.memberNickName,
                    memberId = member.memberId,
                    memberPhoto = member.memberPhoto,
                )
            }
        } else {
            // 설명. redis에서의 데이터도 충분한 경우
            val members = recentLikes.map { queryMemberService.selectMemberByMemberId(it) }

            members.map { member ->
                CommandBoardCommentLikeMemberIdsResponseVO(
                    memberNickname = member.memberNickName,
                    memberId = member.memberId,
                    memberPhoto = member.memberPhoto,
                )
            }
        }
    }

    override fun readBoardCommentLikeCount(boardCommentId: String): Long {
        // 설명. 스크롤 할 때 보일 총 좋아요 수..
        val redisCount = redisTemplate.opsForSet().size("board-comment-like:$boardCommentId") ?: 0

        return if (needAdditionalBoard(boardCommentId, redisCount)) {
            redisCount + readDBLikeCountByBoardId(boardCommentId)
        } else {
            redisCount
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

    private fun readDBLikeCountByMemberId(memberId: String): Long = boardCommentLikeRepository.countByMemberId(memberId)

    private fun readDBLikeCountByBoardId(boardId: String): Long = boardCommentLikeRepository.countByBoardId(boardId)
}