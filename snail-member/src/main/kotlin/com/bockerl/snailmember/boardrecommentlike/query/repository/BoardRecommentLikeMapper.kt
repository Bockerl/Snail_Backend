package com.bockerl.snailmember.boardrecommentlike.query.repository

import org.apache.ibatis.annotations.Mapper

@Mapper
interface BoardRecommentLikeMapper {
    fun selectCountByBoardRecommentId(boardRecommentId: Long): Long
}