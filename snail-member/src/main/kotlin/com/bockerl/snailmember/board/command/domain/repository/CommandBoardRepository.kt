/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */

package com.bockerl.snailmember.board.command.domain.repository

import com.bockerl.snailmember.board.command.domain.aggregate.entity.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommandBoardRepository : JpaRepository<Board, Long>