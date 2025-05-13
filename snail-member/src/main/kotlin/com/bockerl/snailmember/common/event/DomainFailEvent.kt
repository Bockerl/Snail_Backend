package com.bockerl.snailmember.common.event

import java.time.OffsetDateTime
import java.time.ZoneOffset

data class DomainFailEvent(
    val domainName: String, // ex: member, board, boardlike...
    val path: String, // ex: /api/member/profile
    val timeStamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val message: String, // commonEx message
    val cause: String, // compiler ex message
)