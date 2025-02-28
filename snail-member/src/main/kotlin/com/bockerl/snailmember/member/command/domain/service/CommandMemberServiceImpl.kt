package com.bockerl.snailmember.member.command.domain.service

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.service.CommandMemberService
import com.bockerl.snailmember.member.command.domain.repository.MemberRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CommandMemberServiceImpl(private val memberRepository: MemberRepository) : CommandMemberService {
    private val logger = KotlinLogging.logger {}
    override fun putLastAccessTime(memberEmail: String) {
        val member = memberRepository.findMemberByMemberEmail(memberEmail)
            ?: throw CommonException(ErrorCode.NOT_FOUND_MEMBER)

        member.let {
            logger.info { "이메일 로그인 성공 후, 마지막 로그인 시간 업데이트 시작, email: $memberEmail" }
            member.lastAccessTime = LocalDateTime.now()
            memberRepository.save(member)
            logger.info { "마지막 로그인 시간 업데이트 성공" }
        }
    }
}
