package com.bockerl.snailmember.member.command.domain.repository

import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findMemberByMemberEmail(email: String): Member?
}