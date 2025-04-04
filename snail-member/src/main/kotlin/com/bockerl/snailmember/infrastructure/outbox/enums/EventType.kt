package com.bockerl.snailmember.infrastructure.outbox.enums

// 설명. topic 이름을 함께 정의하기
enum class EventType(
    val topic: String,
) {
    LIKE("board-like-events"),
    FILE_CREATED("file-events"),
    FILE_DELETED("file-events"),
}