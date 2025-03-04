package com.bockerl.snailmember.boardrecomment.command.domain.repository

import com.bockerl.snailmember.boardrecomment.command.domain.aggregate.entity.BoardRecomment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandBoardRecommentRepository : JpaRepository<BoardRecomment, Long>