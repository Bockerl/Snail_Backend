package com.bockerl.snailmember.boardcommentlike.command.domain.service

import com.bockerl.snailmember.boardcommentlike.command.application.dto.CommandBoardCommentLikeDTO
import com.bockerl.snailmember.boardcommentlike.command.application.service.CommandBoardCommentLikeService
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.entity.BoardCommentLike
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.enums.BoardCommentLikeActionType
import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event.BoardCommentLikeEvent
import com.bockerl.snailmember.boardcommentlike.command.domain.repository.BoardCommentLikeRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.infrastructure.outbox.dto.OutboxDTO
import com.bockerl.snailmember.infrastructure.outbox.enums.EventType
import com.bockerl.snailmember.infrastructure.outbox.service.OutboxService
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.connection.ReturnType
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class CommandBoardCommentLikeServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val boardCommentLikeRepository: BoardCommentLikeRepository,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper,
) : CommandBoardCommentLikeService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun createBoardCommentLike(commandBoardCommentLikeDTO: CommandBoardCommentLikeDTO) {
        // 설명. 집합으로 각 인덱스 관리. cold data 분리를 위해 expire 설정(1일) -> 호출될 때 마다 갱신됨

        val boardCommentSetKey = "board-comment-like:${commandBoardCommentLikeDTO.boardCommentId}"
        val boardCommentCountKey = "board-comment-like:count:${commandBoardCommentLikeDTO.boardCommentId}"
        val memberSetKey = "board-comment-like:${commandBoardCommentLikeDTO.memberId}"
        val ttlSeconds = Duration.ofDays(1).seconds

        val luaScript =
            """
            local boardCommentSetKey = KEYS[1]
            local boardCommentCountKey = KEYS[2]
            local memberSetKey = KEYS[3]
            local memberId = ARGV[1]
            local boardCommentId = ARGV[2]
            local ttl = tonumber(ARGV[3])
            
            -- boardCommentId에 대한 좋아요 목록에 memberId 추가
            local added = redis.call("SADD", boardCommentSetKey, memberId)
            if added == 1 then
                -- 새로 추가된 경우에만 좋아요 카운터 증가
                redis.call("INCR", boardCommentCountKey)
            end
            -- boardCommentId에 대한 좋아요 목록에 TTL 적용
            redis.call("EXPIRE", boardCommentSetKey, ttl)
            
            -- 역인덱스: memberId에 대한 좋아요한 boardComment 목록에 boardCommentId 추가
            redis.call("SADD", memberSetKey, boardCommentId)
            redis.call("EXPIRE", memberSetKey, ttl)
            
            return added
            """.trimIndent()

        val keys = listOf(boardCommentSetKey, boardCommentCountKey, memberSetKey)
        val args =
            listOf(
                commandBoardCommentLikeDTO.memberId,
                commandBoardCommentLikeDTO.boardCommentId,
                ttlSeconds.toString(),
            )

        if (redisTemplate.execute(
                RedisCallback<Long> { connection ->
                    connection.eval(
                        luaScript.toByteArray(Charsets.UTF_8),
                        ReturnType.INTEGER,
                        keys.size,
                        *keys.map { it.toByteArray(Charsets.UTF_8) }.toTypedArray(),
                        *args.map { it.toByteArray(Charsets.UTF_8) }.toTypedArray(),
                    )
                },
            )
            == 0L
        ) {
            throw CommonException(ErrorCode.ALREADY_LIKED)
        }

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
                idempotencyKey = commandBoardCommentLikeDTO.idempotencyKey,
            )

        outboxService.createOutbox(outbox)
    }

    @Transactional
    override fun createBoardCommentLikeEventList(boardCommentLikeList: List<CommandBoardCommentLikeDTO>) {
        try {
            // 설명. entity형으로 변환은 필요하다.
            val boardCommentLikeListEntities =
                boardCommentLikeList.map { boardCommentLike ->
                    BoardCommentLike(
                        boardId = extractDigits(boardCommentLike.boardId),
                        memberId = extractDigits(boardCommentLike.memberId),
                        boardCommentId = extractDigits(boardCommentLike.boardCommentId),
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
        val boardCommentSetKey = "board-comment-like:${commandBoardCommentLikeDTO.boardCommentId}"
        val boardCommentCountKey = "board-comment-like:count:${commandBoardCommentLikeDTO.boardCommentId}"
        val memberSetKey = "board-comment-like:${commandBoardCommentLikeDTO.memberId}"
        val ttlSeconds = Duration.ofDays(1).seconds

        val luaScript =
            """
            local boardCommentSetKey = KEYS[1]
            local boardCommentCountKey = KEYS[2]
            local memberSetKey = KEYS[3]
            local memberId = ARGV[1]
            local boardCommentId = ARGV[2]
            local ttl = tonumber(ARGV[3])
            
            -- boardCommentId에 대한 좋아요 목록에 memberId 제거
            local removed = redis.call("SREM", boardCommentSetKey, memberId)
            if removed == 1 then
                -- 기존에 등록되어 있었다면, 카운터를 감소시킵니다.
                redis.call("DECR", boardCommentCountKey)
            end
            -- 역인덱스: memberId에 대한 좋아요한 board 목록에서 boardCommentId 제거
            redis.call("SREM", memberSetKey, boardCommentId)
            
            redis.call("EXPIRE", boardCommentSetKey, ttl)
            redis.call("EXPIRE", memberSetKey, ttl)
            
            return removed
            """.trimIndent()

        val keys = listOf(boardCommentSetKey, boardCommentCountKey, memberSetKey)
        val args =
            listOf(
                commandBoardCommentLikeDTO.memberId,
                commandBoardCommentLikeDTO.boardCommentId,
                ttlSeconds.toString(),
            )

        if (redisTemplate.execute(
                RedisCallback<Long> { connection ->
                    connection.eval(
                        luaScript.toByteArray(Charsets.UTF_8),
                        ReturnType.INTEGER,
                        keys.size,
                        *keys.map { it.toByteArray(Charsets.UTF_8) }.toTypedArray(),
                        *args.map { it.toByteArray(Charsets.UTF_8) }.toTypedArray(),
                    )
                },
            )
            == 0L
        ) {
            throw CommonException(ErrorCode.ALREADY_UNLIKED)
        }

        // Kafka에 이벤트 발행
        val event =
            BoardCommentLikeEvent(
                boardId = commandBoardCommentLikeDTO.boardId,
                memberId = commandBoardCommentLikeDTO.memberId,
                boardCommentId = commandBoardCommentLikeDTO.boardCommentId,
                boardCommentLikeActionType = BoardCommentLikeActionType.DELETE,
            )

        val jsonPayload = objectMapper.writeValueAsString(event)

        val outbox =
            OutboxDTO(
                aggregateId = commandBoardCommentLikeDTO.boardId,
                eventType = EventType.LIKE,
                payload = jsonPayload,
                idempotencyKey = commandBoardCommentLikeDTO.idempotencyKey,
            )

        outboxService.createOutbox(outbox)
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