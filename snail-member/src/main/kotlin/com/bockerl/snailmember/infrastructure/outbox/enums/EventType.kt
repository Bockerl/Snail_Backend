package com.bockerl.snailmember.infrastructure.outbox.enums

// 설명. topic 이름을 함께 정의하기
enum class EventType(
    val topic: String,
) {
    AUTH_LOGGING("auth-fail-log-events"),
    LIKE("board-like-events"),
    FILE_CREATED("file-events"),
    FILE_DELETED("file-events"),
    FILE("file-events"),
    MEMBER("member-events"),
    MEMBER_LOGGING("member-log-events"),
    ACTIVITY_AREA("activity-area-events"),
    DOMAIN_FAILED("domain-fail-events"),
}