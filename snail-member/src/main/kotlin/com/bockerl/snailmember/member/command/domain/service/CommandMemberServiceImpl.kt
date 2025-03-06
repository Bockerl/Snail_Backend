package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.area.command.domain.aggregate.entity.ActivityArea
import com.bockerl.snailmember.area.command.domain.aggregate.entity.AreaType
import com.bockerl.snailmember.area.command.domain.repository.ActivityAreaRepository
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.file.command.application.dto.CommandFileDTO
import com.bockerl.snailmember.file.command.application.service.CommandFileService
import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.infrastructure.config.TransactionalConfig
import com.bockerl.snailmember.member.command.application.dto.request.ActivityAreaRequestDTO
import com.bockerl.snailmember.member.command.application.dto.request.ProfileRequestDTO
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class CommandMemberServiceImpl(
    private val memberRepository: MemberRepository,
    private val activityAreaRepository: ActivityAreaRepository,
    private val commandFileService: CommandFileService,
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
            member.apply {
                lastAccessTime = LocalDateTime.now()
            }
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
    override fun postActivityArea(
        memberId: String,
        requestDTO: ActivityAreaRequestDTO,
    ) {
        logger.info { "활동지역 등록 서비스 도착" }
        val member =
            memberRepository.findMemberByMemberId(extractDigits(memberId))
                ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
        val foundedMemberId = member.memberId!!
        val newEmdId = extractDigits(requestDTO.primaryId)
        logger.info { "기존 주 활동지역부터 삭제" }
        activityAreaRepository
            .runCatching {
                deleteByMemberIdAndAreaType(foundedMemberId, AreaType.PRIMARY)
            }.onSuccess {
                logger.info { "기존 주 활동지역 삭제 성공" }
            }.onFailure {
                logger.warn { "기존 주 활동지역 삭제 실패" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }

        logger.info { "새로운 주 활동지역 생성" }
        val primaryId = ActivityArea.ActivityId(foundedMemberId, newEmdId)
        val primaryArea =
            ActivityArea(
                primaryId,
                areaType = AreaType.PRIMARY,
            )
        activityAreaRepository
            .runCatching {
                save(primaryArea)
            }.onSuccess {
                logger.info { "새로운 주 활동지역 저장 성공" }
            }.onFailure {
                logger.warn { "새로운 주 활동지역 저장 실패" }
                throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
            }

        requestDTO.workplaceId?.let {
            logger.info { "직장근처 활동지역 수정 시작" }
            activityAreaRepository
                .runCatching {
                    deleteByMemberIdAndAreaType(foundedMemberId, AreaType.WORKPLACE)
                }.onSuccess {
                    logger.info { "기존 직장근처 활동지역 삭제 성공" }
                }.onFailure {
                    logger.warn { "기존 직장근처 활동지역 삭제 실패" }
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
            logger.info { "새로운 직장근처 활동지역 생성" }
            val newEmdId2 = extractDigits(requestDTO.workplaceId)
            val workplaceId = ActivityArea.ActivityId(foundedMemberId, newEmdId2)
            val workplaceArea =
                ActivityArea(
                    workplaceId,
                    areaType = AreaType.WORKPLACE,
                )
            activityAreaRepository
                .runCatching {
                    save(workplaceArea)
                }.onSuccess {
                    logger.info { "새로운 직장근처 활동지역 저장 성공" }
                }.onFailure {
                    logger.warn { "새로운 직장근처 활동지역 저장 실패" }
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
        }
    }

    override fun patchProfile(
        memberId: String,
        requestDTO: ProfileRequestDTO,
        file: MultipartFile?,
    ): Unit =
        TransactionalConfig.run {
            logger.info { "프로필 수정 서비스 도착" }
            val member =
                memberRepository.findMemberByMemberId(extractDigits(memberId))
                    ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)
            member.apply {
                memberNickname = requestDTO.nickName
                memberBirth = requestDTO.birth
                memberGender = requestDTO.gender
                selfIntroduction = requestDTO.selfIntroduction
            }
            memberRepository
                .runCatching {
                    save(member)
                }.onSuccess {
                    logger.info { "프로필 사진 제외 회원 정보 수정 성공" }
                }.onFailure {
                    logger.warn { "프로플 사진 제외 회원 정보 수정 실패" }
                    throw CommonException(ErrorCode.INTERNAL_SERVER_ERROR)
                }
            file?.let {
                logger.info { "프로필 사진 수정 시작" }
                val commandFileDTO =
                    CommandFileDTO(
                        fileTargetType = FileTargetType.MEMBER,
                        fileTargetId = memberId,
                        memberId = memberId,
                    )
                // 기존 프사가 없다면 생성, 있다면 삭제 후 수정 호출
                if (member.memberPhoto.isBlank()) {
                    commandFileService.createSingleFile(file, commandFileDTO)
                } else {
                    commandFileService.updateProfileImage(file, commandFileDTO)
                }
            }
        }

    private fun extractDigits(input: String): Long = input.filter { it.isDigit() }.toLong()
}