package com.bockerl.snailmember.infrastructure.event.handler

import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaCreateEvent
import com.bockerl.snailmember.area.command.domain.aggregate.event.ActivityAreaUpdateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ActivityAreaEventHandler {
    private val logger = KotlinLogging.logger {}

    fun handleCreate(event: ActivityAreaCreateEvent) {
        // 활동 지역 이벤트의 소비처 = Es(관리자를 위한 인덱싱(통계 + 검색), 로그)
        TODO("Not yet implemented")
    }

    fun handleUpdate(event: ActivityAreaUpdateEvent) {
        // 활동 지역 이벤트의 소비처 = Es(관리자를 위한 인덱싱(통계 + 검색), 로그)
        TODO("Not yet implemented")
    }
}