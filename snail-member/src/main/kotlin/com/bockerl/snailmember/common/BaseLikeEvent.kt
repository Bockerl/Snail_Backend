package com.bockerl.snailmember.common

import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event.BoardCommentLikeEvent
import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BoardLikeEvent::class, name = "BOARD_LIKE"),
    JsonSubTypes.Type(value = BoardCommentLikeEvent::class, name = "BOARD_COMMENT_LIKE"),
)
interface BaseLikeEvent {
    val boardId: String
    val memberId: String
}