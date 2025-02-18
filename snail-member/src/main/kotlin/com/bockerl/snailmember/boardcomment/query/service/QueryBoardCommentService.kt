package com.bockerl.snailmember.boardcomment.query.service

import com.bockerl.snailmember.boardcomment.query.vo.QueryBoardCommentResponseVO

interface QueryBoardCommentService {
    fun getBoardCommentByBoardId(
        boardId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardCommentResponseVO?>

    fun getBoardCommentByMemberId(
        memberId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardCommentResponseVO?>
}