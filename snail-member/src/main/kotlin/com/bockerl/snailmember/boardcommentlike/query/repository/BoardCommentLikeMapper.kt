package com.bockerl.snailmember.boardcommentlike.query.repository

import org.apache.ibatis.annotations.Mapper

@Mapper
interface BoardCommentLikeMapper {
    fun selectCountByBoardCommentId(boardCommentId: Long): Long
}