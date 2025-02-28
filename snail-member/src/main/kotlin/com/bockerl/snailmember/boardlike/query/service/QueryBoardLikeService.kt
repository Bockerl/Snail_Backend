package com.bockerl.snailmember.boardlike.query.service

import com.bockerl.snailmember.board.query.vo.QueryBoardResponseVO
import com.bockerl.snailmember.boardlike.query.vo.QueryBoardLikeMemberIdsResponseVO

interface QueryBoardLikeService {
    fun readBoardLike(boardId: String): List<QueryBoardLikeMemberIdsResponseVO>

    fun readBoardIdsByMemberId(memberId: String): List<QueryBoardResponseVO>

    fun readBoardLikeCount(boardId: String): Long
}