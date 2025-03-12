package com.bockerl.snailmember.boardrecommentlike.query.service

interface QueryBoardRecommentLikeService {
    fun readBoardRecommentLikeCount(boardRecommentId: String): Long
}