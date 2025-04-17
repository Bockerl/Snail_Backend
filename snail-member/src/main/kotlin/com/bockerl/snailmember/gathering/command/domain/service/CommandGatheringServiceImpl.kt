package com.bockerl.snailmember.gathering.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.gathering.command.application.dto.*
import com.bockerl.snailmember.gathering.command.application.service.CommandGatheringService
import com.bockerl.snailmember.gathering.command.domain.aggregate.entity.Gathering
import com.bockerl.snailmember.gathering.command.domain.aggregate.entity.GatheringMember
import com.bockerl.snailmember.gathering.command.domain.aggregate.entity.GatheringMemberId
import com.bockerl.snailmember.gathering.command.domain.enums.GatheringRole
import com.bockerl.snailmember.gathering.command.domain.enums.GatheringType
import com.bockerl.snailmember.gathering.command.domain.repository.CommandGatheringMemberRepository
import com.bockerl.snailmember.gathering.command.domain.repository.CommandGatheringRepository
import jakarta.transaction.Transactional
import org.springframework.data.redis.core.Cursor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CommandGatheringServiceImpl(
    private val commandGatheringRepository: CommandGatheringRepository,
    private val commandFileService: CommandFileService,
    private val redisTemplate: RedisTemplate<String, String>,
    private val commandGatheringMemberRepository: CommandGatheringMemberRepository,
) : CommandGatheringService {
    @Transactional
    override fun createGathering(
        commandGatheringCreateDTO: CommandGatheringCreateDTO,
        files: List<MultipartFile>,
    ) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandGatheringCreateDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

        val gathering =
            Gathering(
                gatheringTitle = commandGatheringCreateDTO.gatheringTitle,
                gatheringInformation = commandGatheringCreateDTO.gatheringInformation,
                gatheringType = commandGatheringCreateDTO.gatheringType,
                gatheringRegion = commandGatheringCreateDTO.gatheringRegion,
                gatheringLimit = commandGatheringCreateDTO.gatheringLimit,
            )

        val gatheringEntity = commandGatheringRepository.save(gathering)

        // 모임장 테이블에 추가
        val gatheringMember =
            gatheringEntity.gatheringId?.let {
                GatheringMember(
                    id =
                        GatheringMemberId(
                            gatheringId = gatheringEntity.gatheringId!!,
                            memberId = extractDigits(commandGatheringCreateDTO.memberId),
                        ),
                    gathering = gatheringEntity,
                    gatheringRole = GatheringRole.ROLE_LEADER,
                )
            }

        gatheringMember?.let {
            commandGatheringMemberRepository.save(it)
        }

        if (files.isNotEmpty()) {
            val commandFileDTO =
                gatheringEntity.gatheringId?.let {
                    CommandFileDTO(
                        fileTargetType = FileTargetType.GATHERING,
                        fileTargetId = formattedGatheringId(it),
                        memberId = commandGatheringCreateDTO.memberId,
                        idempotencyKey = commandGatheringCreateDTO.idempotencyKey,
                    )
                }

            commandFileDTO?.let { commandFileService.createFiles(files, it) }
        }

        redisTemplate.delete("gathering/${commandGatheringCreateDTO.gatheringType}")
        invalidateByType(gathering.gatheringType)

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandGatheringCreateDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    @Transactional
    override fun updateGathering(
        commandGatheringUpdateDTO: CommandGatheringUpdateDTO,
        files: List<MultipartFile>,
    ) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandGatheringUpdateDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

        val gatheringId = extractDigits(commandGatheringUpdateDTO.gatheringId)

        val gathering = commandGatheringRepository.findById(gatheringId).orElseThrow { CommonException(ErrorCode.NOT_FOUND_GATHERING) }

        gathering.apply {
            gatheringInformation = commandGatheringUpdateDTO.gatheringInformation
            gatheringType = commandGatheringUpdateDTO.gatheringType
            gatheringRegion = commandGatheringUpdateDTO.gatheringRegion
            gatheringLimit = commandGatheringUpdateDTO.gatheringLimit
        }

        val gatheringEntity = commandGatheringRepository.save(gathering)

        if (files.isNotEmpty()) {
            val commandFileDTO =
                gatheringEntity.gatheringId?.let {
                    CommandFileDTO(
                        fileTargetType = FileTargetType.GATHERING,
                        fileTargetId = formattedGatheringId(it),
                        memberId = commandGatheringUpdateDTO.memberId,
                        idempotencyKey = commandGatheringUpdateDTO.idempotencyKey,
                    )
                }

            commandFileDTO?.let { commandFileService.updateFiles(it, commandGatheringUpdateDTO.deleteFilesIds, files) }
        }

        redisTemplate.delete("gathering/${commandGatheringUpdateDTO.gatheringType}")
        invalidateByType(gathering.gatheringType)

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandGatheringUpdateDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    @Transactional
    override fun deleteGathering(commandGatheringDeleteDTO: CommandGatheringDeleteDTO) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandGatheringDeleteDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

        val gatheringId = extractDigits(commandGatheringDeleteDTO.gatheringId)
        val gathering = commandGatheringRepository.findById(gatheringId).orElseThrow { CommonException(ErrorCode.NOT_FOUND_GATHERING) }

        gathering.apply {
            active = false
        }

        // 모임 별 모임원도 active = false 해줘야함
//        val gatheringMember =
//            commandGatheringMemberRepository.findByIdGatheringId(gatheringId)
        commandGatheringMemberRepository.updateActiveByGatheringId(gatheringId)

        val commandFileDTO =
            CommandFileDTO(
                fileTargetType = FileTargetType.GATHERING,
                fileTargetId = formattedGatheringId(gatheringId),
                memberId = commandGatheringDeleteDTO.memberId,
                idempotencyKey = commandGatheringDeleteDTO.idempotencyKey,
            )

        commandFileService.deleteFile(commandFileDTO)

        redisTemplate.delete("gathering/${gathering.gatheringType}")
        invalidateByType(gathering.gatheringType)

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandGatheringDeleteDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    @Transactional
    override fun updateGatheringAuthorization(commandGatheringAuthorizationUpdateDTO: CommandGatheringAuthorizationUpdateDTO) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandGatheringAuthorizationUpdateDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

        val gatheringId = extractDigits(commandGatheringAuthorizationUpdateDTO.gatheringId)
        val memberId = extractDigits(commandGatheringAuthorizationUpdateDTO.memberId)

        val compositeKey =
            GatheringMemberId(
                gatheringId = gatheringId,
                memberId = memberId,
            )

        val gatheringMember =
            commandGatheringMemberRepository
                .findById(
                    compositeKey,
                ).orElseThrow { CommonException(ErrorCode.NOT_FOUND_GATHERING) }

        gatheringMember.apply {
            gatheringRole = commandGatheringAuthorizationUpdateDTO.gatheringRole
        }

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandGatheringAuthorizationUpdateDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    @Transactional
    override fun createGatheringMember(commandGatheringMemberCreateDTO: CommandGatheringMemberCreateDTO) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandGatheringMemberCreateDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

        val gatheringId = extractDigits(commandGatheringMemberCreateDTO.gatheringId)
        val memberId = extractDigits(commandGatheringMemberCreateDTO.memberId)

        val gathering = commandGatheringRepository.findById(gatheringId).orElseThrow { CommonException(ErrorCode.NOT_FOUND_GATHERING) }

        val gatheringMember =
            GatheringMember(
                id =
                    GatheringMemberId(
                        gatheringId = gatheringId,
                        memberId = memberId,
                    ),
                gathering = gathering,
                gatheringRole = GatheringRole.ROLE_MEMBER,
            )

        gatheringMember.let {
            commandGatheringMemberRepository.save(it)
        }

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandGatheringMemberCreateDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    override fun deleteGatheringMember(commandGatheringMemberCreateDTO: CommandGatheringMemberCreateDTO) {
        // 따닥 방지
        if (redisTemplate.hasKey(commandGatheringMemberCreateDTO.idempotencyKey)) {
            throw CommonException(ErrorCode.ALREADY_REQUESTED)
        }

        val gatheringId = extractDigits(commandGatheringMemberCreateDTO.gatheringId)
        val memberId = extractDigits(commandGatheringMemberCreateDTO.memberId)

        val compositeKey =
            GatheringMemberId(
                gatheringId = gatheringId,
                memberId = memberId,
            )

        val gatheringMember =
            commandGatheringMemberRepository
                .findById(
                    compositeKey,
                ).orElseThrow { CommonException(ErrorCode.NOT_FOUND_GATHERING) }

        gatheringMember.apply {
            active = false
        }

        val idempotencyScript =
            """
            local res = redis.call("set", KEYS[1], ARGV[1], "EX", ARGV[2])
            return res
            """.trimIndent()

        val idempotencyRedisScript = DefaultRedisScript<String>(idempotencyScript, String::class.java)
        val ttlInSeconds = "3600" // 1시간 TTL

        val result =
            redisTemplate.execute(
                idempotencyRedisScript,
                listOf(commandGatheringMemberCreateDTO.idempotencyKey),
                "PROCESSED",
                ttlInSeconds,
            )

        // result가 "OK"이면 SET 명령이 성공적으로 실행된 것입니다.
        if (result != "OK") {
            throw CommonException(ErrorCode.REDIS_ERROR)
        }
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()

    private fun formattedGatheringId(gatheringId: Long): String = "GAT-${gatheringId.toString().padStart(8, '0') ?: "00000000"}"

    // 설명. 점진적으로 커서를 이용해 키를 검색한다. (현재는 1000개씩 scan)
    private fun scanAndDelete(pattern: String) {
        // ScanOptions를 통해 매칭 패턴과 count(한번에 조회할 키 수)를 설정합니다.
        val scanOptions =
            ScanOptions
                .scanOptions()
                .match(pattern)
                .count(1000)
                .build()
        // SCAN 커서를 엽니다.
        val cursor: Cursor<String> = redisTemplate.scan(scanOptions)
        cursor.use {
            while (it.hasNext()) {
                val key = it.next()
                redisTemplate.delete(key)
            }
        }
    }

    private fun invalidateByType(type: GatheringType) {
        val pattern = "gathering/*$type*"
        scanAndDelete(pattern)
    }
}