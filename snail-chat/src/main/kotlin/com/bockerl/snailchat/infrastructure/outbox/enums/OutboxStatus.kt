package com.bockerl.snailchat.infrastructure.outbox.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Outbox 처리 상태", enumAsRef = true)
enum class OutboxStatus {
    @Schema(description = "이벤트가 생성되었고 아직 처리되지 않음")
    PENDING,

    @Schema(description = "현재 처리 중 (중복 처리 방지를 위함)")
    PROCESSING,

    @Schema(description = "이벤트 전송 성공 (Kafka 등)")
    SUCCESS,

    @Schema(description = "이벤트 전송 실패 (예외 발생)")
    FAILURE,

    @Schema(description = "재시도 중 (FAILURE 상태에서 옮겨옴)")
    RETRYING,

    @Schema(description = "지속적인 실패로 더 이상 재처리하지 않음")
    DEAD,
}