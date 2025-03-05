package com.bockerl.snailmember.boardrecommentlike.command.domain.service

import com.bockerl.snailmember.boardrecommentlike.command.application.dto.CommandBoardRecommentLikeDTO
import com.bockerl.snailmember.boardrecommentlike.command.application.service.CommandBoardRecommentLikeService
import com.bockerl.snailmember.boardrecommentlike.command.domain.aggregate.entity.BoardRecommentLike
import com.bockerl.snailmember.boardrecommentlike.command.domain.aggregate.enums.BoardRecommentLikeActionType
import com.bockerl.snailmember.boardrecommentlike.command.domain.aggregate.event.BoardRecommentLikeEvent
import com.bockerl.snailmember.boardrecommentlike.command.domain.repository.BoardRecommentLikeRepository
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
class CommandBoardRecommentLikeServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val boardRecommentLikeRepository: BoardRecommentLikeRepository,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper,
) : CommandBoardRecommentLikeService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun createBoardRecommentLike(commandBoardRecommentLikeDTO: CommandBoardRecommentLikeDTO) {
        // 설명. 집합으로 각 인덱스 관리. cold data 분리를 위해 expire 설정(1일) -> 호출될 때 마다 갱신됨

        val boardRecommentSetKey = "board-recomment-like:${commandBoardRecommentLikeDTO.boardRecommentId}"
        val boardRecommentCountKey = "board-recomment-like:count:${commandBoardRecommentLikeDTO.boardRecommentId}"
        val memberSetKey = "board-recomment-like:${commandBoardRecommentLikeDTO.memberId}"
        val ttlSeconds = Duration.ofDays(1).seconds

        val luaScript =
            """
            local boardRecommentSetKey = KEYS[1]
            local boardRecommentCountKey = KEYS[2]
            local memberSetKey = KEYS[3]
            local memberId = ARGV[1]
            local boardRecommentId = ARGV[2]
            local ttl = tonumber(ARGV[3])
            
            -- boardRecommentId에 대한 좋아요 목록에 memberId 추가
            local added = redis.call("SADD", boardRecommentSetKey, memberId)
            if added == 1 then
                -- 새로 추가된 경우에만 좋아요 카운터 증가
                redis.call("INCR", boardRecommentCountKey)
            end
            -- boardCommentId에 대한 좋아요 목록에 TTL 적용
            redis.call("EXPIRE", boardRecommentSetKey, ttl)
            
            -- 역인덱스: memberId에 대한 좋아요한 boardRecomment 목록에 boardRecommentId 추가
            redis.call("SADD", memberSetKey, boardRecommentId)
            redis.call("EXPIRE", memberSetKey, ttl)
            
            return added
            """.trimIndent()

        val keys = listOf(boardRecommentSetKey, boardRecommentCountKey, memberSetKey)
        val args =
            listOf(
                commandBoardRecommentLikeDTO.memberId,
                commandBoardRecommentLikeDTO.boardRecommentId,
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
            BoardRecommentLikeEvent(
                boardId = commandBoardRecommentLikeDTO.boardId,
                memberId = commandBoardRecommentLikeDTO.memberId,
                boardCommentId = commandBoardRecommentLikeDTO.boardCommentId,
                boardRecommentId = commandBoardRecommentLikeDTO.boardRecommentId,
                boardRecommentLikeActionType = BoardRecommentLikeActionType.CREATE,
            )

        val jsonPayload = objectMapper.writeValueAsString(event)

        val outbox =
            OutboxDTO(
                aggregateId = commandBoardRecommentLikeDTO.boardRecommentId,
                eventType = EventType.LIKE,
                payload = jsonPayload,
            )

        outboxService.createOutbox(outbox)
    }

    @Transactional
    override fun createBoardRecommentLikeEventList(boardRecommentLikeList: List<CommandBoardRecommentLikeDTO>) {
        try {
            // 설명. entity형으로 변환은 필요하다.
            val boardRecommentLikeListEntities =
                boardRecommentLikeList.map { boardRecommentLike ->
                    BoardRecommentLike(
                        boardId = extractDigits(boardRecommentLike.boardId),
                        memberId = extractDigits(boardRecommentLike.memberId),
                        boardCommentId = extractDigits(boardRecommentLike.boardCommentId),
                        boardRecommentId = extractDigits(boardRecommentLike.boardRecommentId),
                    )
                }

            boardRecommentLikeRepository.saveAll(boardRecommentLikeListEntities)
        } catch (ex: DataIntegrityViolationException) {
            logger.error { "Bulk insert 실패: ${ex.message}. 개별 처리 시도합니다." }
            boardRecommentLikeList.forEach { event ->
                val boardRecommentLikeEntity =
                    BoardRecommentLike(
                        boardId = extractDigits(event.boardId),
                        memberId = extractDigits(event.memberId),
                        boardCommentId = extractDigits(event.boardCommentId),
                        boardRecommentId = extractDigits(event.boardRecommentId),
                    )
                try {
                    boardRecommentLikeRepository.save(boardRecommentLikeEntity)
                } catch (innerEx: DataIntegrityViolationException) {
                    logger.error { "개별 처리 실패(duplicate key 처리): $event, $innerEx" }
                } catch (otherEx: Exception) {
                    logger.error { "개별 처리 실패: $event, $otherEx" }
                }
            }
        }
    }

    @Transactional
    override fun deleteBoardRecommentLike(commandBoardRecommentLikeDTO: CommandBoardRecommentLikeDTO) {
        val boardRecommentSetKey = "board-recomment-like:${commandBoardRecommentLikeDTO.boardRecommentId}"
        val boardRecommentCountKey = "board-recomment-like:count:${commandBoardRecommentLikeDTO.boardRecommentId}"
        val memberSetKey = "board-recomment-like:${commandBoardRecommentLikeDTO.memberId}"
        val ttlSeconds = Duration.ofDays(1).seconds

        val luaScript =
            """
            local boardRecommentSetKey = KEYS[1]
            local boardRecommentCountKey = KEYS[2]
            local memberSetKey = KEYS[3]
            local memberId = ARGV[1]
            local boardRecommentId = ARGV[2]
            local ttl = tonumber(ARGV[3])
            
            -- boardRecommentId에 대한 좋아요 목록에 memberId 제거
            local removed = redis.call("SREM", boardRecommentSetKey, memberId)
            if removed == 1 then
                -- 기존에 등록되어 있었다면, 카운터를 감소시킵니다.
                redis.call("DECR", boardRecommentCountKey)
            end
            -- 역인덱스: memberId에 대한 좋아요한 board 목록에서 boardCommentId 제거
            redis.call("SREM", memberSetKey, boardRecommentId)
            
            redis.call("EXPIRE", boardRecommentSetKey, ttl)
            redis.call("EXPIRE", memberSetKey, ttl)
            
            return removed
            """.trimIndent()

        val keys = listOf(boardRecommentSetKey, boardRecommentCountKey, memberSetKey)
        val args =
            listOf(
                commandBoardRecommentLikeDTO.memberId,
                commandBoardRecommentLikeDTO.boardRecommentId,
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
            BoardRecommentLikeEvent(
                boardId = commandBoardRecommentLikeDTO.boardId,
                memberId = commandBoardRecommentLikeDTO.memberId,
                boardCommentId = commandBoardRecommentLikeDTO.boardCommentId,
                boardRecommentLikeActionType = BoardRecommentLikeActionType.DELETE,
                boardRecommentId = commandBoardRecommentLikeDTO.boardRecommentId,
            )

        val jsonPayload = objectMapper.writeValueAsString(event)

        val outbox =
            OutboxDTO(
                aggregateId = commandBoardRecommentLikeDTO.boardId,
                eventType = EventType.LIKE,
                payload = jsonPayload,
            )

        outboxService.createOutbox(outbox)
    }

    @Transactional
    override fun deleteBoardRecommentLikeEvent(commandBoardRecommentLikeDTO: CommandBoardRecommentLikeDTO) {
        boardRecommentLikeRepository.deleteByMemberIdAndBoardRecommentId(
            extractDigits(commandBoardRecommentLikeDTO.memberId),
            extractDigits(commandBoardRecommentLikeDTO.boardRecommentId),
        )
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}