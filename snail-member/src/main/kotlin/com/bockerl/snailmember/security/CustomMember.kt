package com.bockerl.snailmember.security

import com.bockerl.snailmember.member.command.domain.aggregate.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomMember(
    member: Member,
    authorities: Collection<GrantedAuthority>,
) : User(
        member.memberEmail,
        member.memberPassword,
        authorities,
    ) {
    val memberId: String = member.formattedId
    val memberNickname: String = member.memberNickname
    val memberEmail: String = member.memberEmail
    val memberPhoto: String = member.memberPhoto
}