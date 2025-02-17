package com.bockerl.snailmember.boardcomment.query.controller

import com.bockerl.snailmember.boardcomment.query.service.QueryBoardCommentService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/board-comment")
class QueryBoardCommentController(
    private val queryBoardCommentService: QueryBoardCommentService
) {

}