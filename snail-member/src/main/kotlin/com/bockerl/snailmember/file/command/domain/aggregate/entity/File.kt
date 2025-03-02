/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.file.command.domain.aggregate.entity

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "File")
data class File(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "fil_seq_generator", // 사용할 generator 이름
    )
    @SequenceGenerator(
        name = "fil_seq_generator", // generator 이름
        sequenceName = "fil", // db seq 이름
        allocationSize = 1, // seq 증가량 (추후에 성능에 따라 변경해야 할지도 모름)
    )
    var fileId: Long? = null,
    @Column(name = "file_name", nullable = false, length = 255)
    var fileName: String,
    @Column(name = "file_type", nullable = false, length = 255)
    var fileType: String,
    @Column(name = "file_url", nullable = false, length = 255)
    var fileUrl: String,
    @Column(name = "active", nullable = false)
    var active: Boolean = true,
    // 설명. fk인 회원 번호
    @Column(name = "member_id", nullable = false)
    var memberId: Long? = null,
    // 설명. 어떤 도메인인지.. enum 타입으로(신고 도메인과 같이)
    @Column(name = "file_target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var fileTargetType: FileTargetType? = null,
    // 설명. target pk가 들어갈 예정
    @Column(name = "file_target_id", nullable = false)
    var fileTargetId: Long? = null,
) {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    lateinit var createdAt: LocalDateTime

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    val formattedId: String
        get() = "FIL-${fileId?.toString()?.padStart(8, '0') ?: "00000000"}"
}