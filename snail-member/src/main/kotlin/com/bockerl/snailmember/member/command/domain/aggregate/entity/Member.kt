/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.member.command.domain.aggregate.entity

import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Gender
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.Language
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.MemberStatus
import com.bockerl.snailmember.member.command.domain.aggregate.entity.enums.SignUpPath
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "member")
@EntityListeners(AuditingEntityListener::class)
data class Member(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "mem_seq_generator", // 사용할 generator 이름
    )
    @SequenceGenerator(
        name = "mem_seq_generator", // generator 이름
        sequenceName = "mem", // db seq 이름
        allocationSize = 1, // seq 증가량 (추후에 성능에 따라 변경해야 할지도 모름)
    )
    var memberId: Long? = null,
    @Column(name = "member_email", length = 255, nullable = false, unique = true)
    var memberEmail: String,
    @Column(name = "member_phone_number", nullable = false, length = 255, unique = true)
    var memberPhoneNumber: String,
    @Column(name = "member_nickname", length = 255, nullable = false)
    var memberNickname: String,
    @Column(name = "member_password", length = 255, nullable = false)
    var memberPassword: String,
    @Column(name = "member_photo", length = 255, nullable = true)
    var memberPhoto: String,
    @Column(name = "member_status", nullable = false)
    @Enumerated(EnumType.STRING)
    var memberStatus: MemberStatus,
    @Column(name = "member_language", nullable = true)
    @Enumerated(EnumType.STRING)
    var memberLanguage: Language,
    @Column(name = "member_gender", nullable = true)
    @Enumerated(EnumType.STRING)
    var memberGender: Gender,
    @Column(name = "member_region", nullable = false)
    var memberRegion: String,
    @Column(name = "member_birth", nullable = false)
    var memberBirth: LocalDate,
    @Column(name = "signup_path", nullable = false)
    @Enumerated(EnumType.STRING)
    val signupPath: SignUpPath,
    @Column(name = "self_introduction", length = 330, nullable = true)
    var selfIntroduction: String,
) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    @Column(name = "last_access_time", nullable = false)
    var lastAccessTime: LocalDateTime? = LocalDateTime.now()

    val formattedId: String
        get() = "MEM-${memberId?.toString()?.padStart(8, '0') ?: "00000000"}"
}