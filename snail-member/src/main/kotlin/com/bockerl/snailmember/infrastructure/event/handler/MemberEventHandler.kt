package com.bockerl.snailmember.infrastructure.event.handler

import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberCreateEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberDeleteEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberLoginEvent
import com.bockerl.snailmember.member.command.domain.aggregate.event.MemberUpdateEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class MemberEventHandler {
    private val logger = KotlinLogging.logger {}

    fun handleCreate(event: MemberCreateEvent) {
        // 멤버 생성 이벤트의 소비처 = ES(새로운 검색 인덱싱, 로깅), Mongo(채팅에 검색?)
        TODO("Not yet implemented")
    }

    fun handleLogin(event: MemberLoginEvent) {
        // 멤버 로그인 이벤트의 소비처 = ES(로깅)
        TODO("Not yet implemented")
    }

    fun handleUpdate(event: MemberUpdateEvent) {
        // 멤버 업데이트 이벤트의 소비처 = Es(새로운 인덱싱, 로깅), Mongo(실시간 채팅 반영)
        TODO("Not yet implemented")
    }

    fun handleDelete(event: MemberDeleteEvent) {
        // 멤버 탈퇴 이벤트의 소비처 = Es(인덱싱 삭제, 로깅), Mongo(실시간 채팅 반영)
        TODO("Not yet implemented")
    }
}