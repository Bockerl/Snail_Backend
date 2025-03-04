package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.area.command.domain.aggregate.entity.ActivityArea
import com.bockerl.snailmember.area.command.domain.aggregate.entity.AreaType
import com.bockerl.snailmember.area.command.domain.repository.ActivityAreaRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CommandMemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val activityAreaRepository: ActivityAreaRepository,
) : CommandMemberService {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun putLastAccessTime(memberEmail: String) {
        logger.info { "마지막 접속 시간 업데이트 서비스 도착" }
        val member =
            memberRepository.findMemberByMemberEmail(memberEmail)
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)

        member.let {
            logger.info { "이메일 로그인 성공 후, 마지막 로그인 시간 업데이트 시작, email: $memberEmail" }
            member.updateLastAccessTime(LocalDateTime.now())
            memberRepository
                .runCatching {
                    memberRepository.save(member)
                }.onSuccess {
                    logger.info { "마지막 로그인 시간 업데이트 성공 - email: $memberEmail" }
                }.onFailure {
                    logger.error { "마지막 로그인 시간 업데이트 실패 - email: $memberEmail" }
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
        }
    }

    @Transactional
    override fun postActivityArea(requestDTO: ActivityAreaRequestDTO) {
        logger.info { "활동지역 등록 서비스 도착" }
        val member =
            memberRepository.findMemberByMemberId(extractDigits(requestDTO.memberId))
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        val primaryArea =
            ActivityArea(
                id =
                    ActivityArea.ActivityId(
                        memberId = member.memberId!!,
                        emdAreasId = extractDigits(requestDTO.primaryId),
                    ),
                areaType = AreaType.PRIMARY,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        activityAreaRepository
            .runCatching {
                logger.info { "주 활동지역 저장 시작" }
                save(primaryArea)
            }.onSuccess {
                logger.info { "주 활동지역 저장 성공" }
            }.onFailure {
                logger.warn { "주 활동지역 저장 실패 - 지역정보: $primaryArea" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }
        requestDTO.workplaceId?.let {
            val workplaceArea =
                ActivityArea(
                    id =
                        ActivityArea.ActivityId(
                            memberId = member.memberId!!,
                            emdAreasId = extractDigits(requestDTO.workplaceId),
                        ),
                    areaType = AreaType.WORKPLACE,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                )
            activityAreaRepository
                .runCatching {
                    logger.info { "직장 근처 활동지역 저장 시작" }
                    save(workplaceArea)
                }.onSuccess {
                    logger.info { "직장 근처 활동지역 저장 성공" }
                }.onFailure {
                    logger.warn { "직장 근처 활동지역 저장 실패 - 지역 정보: $workplaceArea" }
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
        }
    }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}