package com.bockerl.snailmember.boardrecomment.query.service

import com.bockerl.snailmember.boardrecomment.query.vo.QueryBoardRecommentResponseVO

interface QueryBoardRecommentService {
    fun getBoardRecommentByBoardCommentId(
        boardCommentId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardRecommentResponseVO?>

    fun getBoardRecommentByMemberId(
        memberId: String,
        lastId: Long?,
        pageSize: Int,
    ): List<QueryBoardRecommentResponseVO?>
}