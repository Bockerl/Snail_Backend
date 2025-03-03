package com.bockerl.snailmember.boardlike.command.domain.service

import com.bockerl.snailmember.boardlike.command.application.dto.CommandBoardLikeDTO
import com.bockerl.snailmember.boardlike.command.application.service.CommandBoardLikeService
import com.bockerl.snailmember.boardlike.command.domain.aggregate.entity.BoardLike
import com.bockerl.snailmember.boardlike.command.domain.aggregate.enum.BoardLikeActionType
import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import com.bockerl.snailmember.boardlike.command.domain.repository.BoardLikeRepository
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
class CommandBoardLikeServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val boardLikeRepository: BoardLikeRepository,
    private val outboxService: OutboxService,
    private val objectMapper: ObjectMapper,
) : CommandBoardLikeService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun createBoardLike(commandBoardLikeDTO: CommandBoardLikeDTO) {
        // 설명. redis에서 board pk 기준 인덱스 설정 및 member pk 기준 인덱스 설정 할 것
        // 설명. 집합으로 각 인덱스 관리. cold data 분리를 위해 expire 설정(1일) -> 호출될 때 마다 갱신됨

        val boardSetKey = "board-like:${commandBoardLikeDTO.boardId}"
        val boardCountKey = "board-like:count:${commandBoardLikeDTO.boardId}"
        val memberSetKey = "board-like:${commandBoardLikeDTO.memberId}"
        val ttlSeconds = Duration.ofDays(1).seconds

        val luaScript =
            """
            local boardSetKey = KEYS[1]
            local boardCountKey = KEYS[2]
            local memberSetKey = KEYS[3]
            local memberId = ARGV[1]
            local boardId = ARGV[2]
            local ttl = tonumber(ARGV[3])
            
            -- boardId에 대한 좋아요 목록에 memberId 추가
            local added = redis.call("SADD", boardSetKey, memberId)
            if added == 1 then
                -- 새로 추가된 경우에만 좋아요 카운터 증가
                redis.call("INCR", boardCountKey)
            end
            -- boardId에 대한 좋아요 목록에 TTL 적용
            redis.call("EXPIRE", boardSetKey, ttl)
            
            -- 역인덱스: memberId에 대한 좋아요한 board 목록에 boardId 추가
            redis.call("SADD", memberSetKey, boardId)
            redis.call("EXPIRE", memberSetKey, ttl)
            
            return added
            """.trimIndent()

        val keys = listOf(boardSetKey, boardCountKey, memberSetKey)
        val args =
            listOf(
                commandBoardLikeDTO.memberId,
                commandBoardLikeDTO.boardId,
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

        // Outbox에 이벤트 발행
        val event =
            BoardLikeEvent(
                boardId = commandBoardLikeDTO.boardId,
                memberId = commandBoardLikeDTO.memberId,
                boardLikeActionType = BoardLikeActionType.CREATE,
            )

        val jsonPayload = objectMapper.writeValueAsString(event)

        val outbox =
            OutboxDTO(
                aggregateId = commandBoardLikeDTO.boardId,
                eventType = EventType.LIKE,
                payload = jsonPayload,
            )

        outboxService.createOutbox(outbox)
    }

    @Transactional
    override fun createBoardLikeEventList(boardLikeList: List<CommandBoardLikeDTO>) {
        try {
            // 설명. entity형으로 변환은 필요하다.
            val boardLikeListEntities =
                boardLikeList.map { boardLike ->
                    BoardLike(
                        boardId = extractDigits(boardLike.boardId),
                        memberId = extractDigits(boardLike.memberId),
                    )
                }

            boardLikeRepository.saveAll(boardLikeListEntities)
        } catch (ex: DataIntegrityViolationException) {
            logger.error { "Bulk insert 실패: ${ex.message}. 개별 처리 시도합니다." }
            boardLikeList.forEach { event ->
                val boardLikeEntity =
                    BoardLike(
                        boardId = extractDigits(event.boardId),
                        memberId = extractDigits(event.memberId),
                    )
                try {
                    boardLikeRepository.save(boardLikeEntity)
                } catch (innerEx: DataIntegrityViolationException) {
                    // 무시해도 됨
                    logger.error { "개별 처리 실패(duplicate key 처리): $event, $innerEx" }
                } catch (otherEx: Exception) {
                    logger.error { "개별 처리 실패: $event, $otherEx" }
                    // 설명. 기타 에러에 대해서는 재시도 하지 않고 별도 로직을 수행
                }
            }
        }
    }

    @Transactional
    override fun deleteBoardLike(commandBoardLikeDTO: CommandBoardLikeDTO) {
        val boardSetKey = "board-like:${commandBoardLikeDTO.boardId}"
        val boardCountKey = "board-like:count:${commandBoardLikeDTO.boardId}"
        val memberSetKey = "board-like:${commandBoardLikeDTO.memberId}" // 역인덱스: 멤버별 좋아요 board 목록
        val ttlSeconds = Duration.ofDays(1).seconds

        val luaScript =
            """
            local boardSetKey = KEYS[1]
            local boardCountKey = KEYS[2]
            local memberSetKey = KEYS[3]
            local memberId = ARGV[1]
            local boardId = ARGV[2]
            local ttl = tonumber(ARGV[3])
            
            -- boardId에 대한 좋아요 목록에서 memberId 제거
            local removed = redis.call("SREM", boardSetKey, memberId)
            if removed == 1 then
                -- 기존에 등록되어 있었다면, 카운터를 감소시킵니다.
                redis.call("DECR", boardCountKey)
            end
            -- 역인덱스: memberId에 대한 좋아요한 board 목록에서 boardId 제거
            redis.call("SREM", memberSetKey, boardId)
            -- 두 키 모두 TTL을 재설정합니다.
            redis.call("EXPIRE", boardSetKey, ttl)
            redis.call("EXPIRE", memberSetKey, ttl)
            return removed
            """.trimIndent()

        val keys = listOf(boardSetKey, boardCountKey, memberSetKey)
        val args =
            listOf(
                commandBoardLikeDTO.memberId,
                commandBoardLikeDTO.boardId,
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

        // Outbox에 이벤트 발행
        val event =
            BoardLikeEvent(
                boardId = commandBoardLikeDTO.boardId,
                memberId = commandBoardLikeDTO.memberId,
                boardLikeActionType = BoardLikeActionType.DELETE,
            )

        val jsonPayload = objectMapper.writeValueAsString(event)

        val outbox =
            OutboxDTO(
                aggregateId = commandBoardLikeDTO.boardId,
                eventType = EventType.LIKE,
                payload = jsonPayload,
            )

        outboxService.createOutbox(outbox)
    }

    @Transactional
    override fun deleteBoardLikeEvent(commandBoardLikeDTO: CommandBoardLikeDTO) {
        boardLikeRepository.deleteByMemberIdAndBoardId(
            extractDigits(commandBoardLikeDTO.memberId),
            extractDigits(commandBoardLikeDTO.boardId),
        )
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}