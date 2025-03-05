package com.bockerl.snailmember.boardcommentlike.query.service

interface QueryBoardCommentLikeService {
    fun readBoardCommentLikeCount(boardCommentId: String): Long
}