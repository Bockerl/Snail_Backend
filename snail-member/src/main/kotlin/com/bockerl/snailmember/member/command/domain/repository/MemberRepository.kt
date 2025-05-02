package com.bockerl.snailmember.member.command.domain.repository

import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findMemberByMemberEmailAndMemberStatusNot(
        email: String,
        memberStatus: MemberStatus,
    ): Member?

    fun findMemberByMemberIdAndMemberStatusNot(
        memberId: Long,
        memberStatus: MemberStatus,
    ): Member?
}