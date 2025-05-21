package com.bockerl.snailmember.common.event

import com.bockerl.snailmember.file.command.domain.aggregate.enums.FileTargetType
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileDeletedEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.FileEvent
import com.bockerl.snailmember.file.command.domain.aggregate.event.GatheringFileEvent
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

// JSON으로 직렬화할 때,
// 객체의 실제 타입을 "eventType"이라는 속성에 추가하여 전송하며,
// 역직렬화 시 이 값을 바탕으로 어떤 구현체 클래스에 매핑할지 정함
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = FileEvent::class, name = "FILE"),
    JsonSubTypes.Type(value = FileEvent::class, name = "FILE_CREATED"),
    JsonSubTypes.Type(value = GatheringFileEvent::class, name = "GATHERING_FILE"),
    JsonSubTypes.Type(value = FileDeletedEvent::class, name = "FILE_DELETED"),
)
interface BaseFileEvent {
//    val fileName: String
//    val fileUrl: String
//    val fileType: String
    val fileTargetType: FileTargetType
    val fileTargetId: String
//    val memberId: String
}