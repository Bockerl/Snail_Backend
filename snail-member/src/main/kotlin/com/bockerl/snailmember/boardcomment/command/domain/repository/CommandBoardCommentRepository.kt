package com.bockerl.snailmember.boardcomment.command.domain.repository

import com.bockerl.snailmember.boardcomment.command.domain.aggregate.entity.BoardComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandBoardCommentRepository : JpaRepository<BoardComment, Long>