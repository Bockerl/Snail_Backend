package com.bockerl.snailmember.boardcommentlike.command.domain.service

import com.bockerl.snailmember.board.query.service.QueryBoardService
import com.bockerl.snailmember.boardcommentlike.command.application.dto.CommandBoardCommentLikeDTO
import com.bockerl.snailmember.boardcommentlike.command.application.service.CommandBoardCommentLikeService
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity.BoardCommentLike
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.enum.BoardCommentLikeActionType
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event.BoardCommentLikeEvent
import com.bockerl.snailmember.boardcommentlike.command.domain.repository.BoardCommentLikeRepository
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.bockerl.snailmember.member.query.service.QueryMemberService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
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
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper,
) : CommandBoardCommentLikeService {
    private val logger = KotlinLogging.logger {}

    @Transactional
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
                boardCommentLikeActionType = BoardCommentLikeActionType.CREATE,
            )

        val jsonPayload = objectMapper.writeValueAsString(event)

        val outbox =
            OutboxDTO(
                aggregateId = commandBoardCommentLikeDTO.boardCommentId,
                eventType = EventType.LIKE,
                payload = jsonPayload,
            )

//        kafkaTemplate.send("board-like-events", event)
//        kafkaBoardCommentLikeTemplate.send("board-comment-like-events", event)
        outboxService.createOutbox(outbox)
    }

    @Transactional
    override fun createBoardCommentLikeEventList(boardCommentLikeList: List<CommandBoardCommentLikeDTO>) {
        try {
            // 설명. entity형으로 변환은 필요하다.
            val boardCommentLikeListEntities =
                boardCommentLikeList.map { boardLike ->
                    BoardCommentLike(
                        boardId = extractDigits(boardLike.boardId),
                        memberId = extractDigits(boardLike.memberId),
                        boardCommentId = extractDigits(boardLike.boardCommentId),
                    )
                }

            boardCommentLikeRepository.saveAll(boardCommentLikeListEntities)
        } catch (ex: DataIntegrityViolationException) {
            logger.error("Bulk insert 실패: ${ex.message}. 개별 처리 시도합니다.")
            boardCommentLikeList.forEach { event ->
                val boardCommentLikeEntity =
                    BoardCommentLike(
                        boardId = extractDigits(event.boardId),
                        memberId = extractDigits(event.memberId),
                        boardCommentId = extractDigits(event.boardCommentId),
                    )
                try {
                    boardCommentLikeRepository.save(boardCommentLikeEntity)
                } catch (innerEx: DataIntegrityViolationException) {
                    logger.error { "개별 처리 실패(duplicate key 처리): $event, $innerEx" }
                } catch (otherEx: Exception) {
                    logger.error { "개별 처리 실패: $event, $otherEx" }
                }
            }
        }
    }

    @Transactional
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
                boardCommentLikeActionType = BoardCommentLikeActionType.DELETE,
            )

        kafkaBoardCommentLikeTemplate.send("board-comment-like-events", event)
    }

    @Transactional
    override fun deleteBoardCommentLikeEvent(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO) {
        boardCommentLikeRepository.deleteByMemberIdAndBoardCommentId(
            extractDigits(commandBoardCommentLikeDTO.memberId),
            extractDigits(commandBoardCommentLikeDTO.boardCommentId),
        )
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}