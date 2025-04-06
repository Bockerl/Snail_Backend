package com.bockerl.snailmember.common.event

import com.bockerl.snailmember.boardcommentlike.command.domain.aggregate.event.BoardCommentLikeEvent
import com.bockerl.snailmember.boardlike.command.domain.aggregate.event.BoardLikeEvent
import com.bockerl.snailmember.boardrecommentlike.command.domain.aggregate.event.BoardRecommentLikeEvent
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
    JsonSubTypes.Type(value = BoardRecommentLikeEvent::class, name = "BOARD_RECOMMENT_LIKE"),
)
interface BaseLikeEvent {
    val boardId: String
    val memberId: String
}