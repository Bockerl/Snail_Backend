package com.bockerl.snailmember.member.command.application.dto.request

import java.sql.Timestamp

class EmailRequestDTO(val memberNickName: String, val memberEmail: String, val memberBirth: Timestamp)
