@file:Suppress("ktlint:standard:no-empty-file")

package com.bockerl.snailmember.member.command.application.controller
//
// import com.bockerl.snailmember.common.ResponseDTO
// import org.springframework.web.bind.annotation.PostMapping
// import org.springframework.web.bind.annotation.RequestBody
// import org.springframework.web.bind.annotation.RequestMapping
// import org.springframework.web.bind.annotation.RestController
//
// @RestController
// @RequestMapping("/api/auth")
// class AuthController(
//    private val authService: AuthService,
//    private val authConverter: AuthConverter,
// ) {
//    @PostMapping("/verification/trial/email")
//    fun sendEmailVerficationCode(
//        @RequestBody emailRequestVO: EmailRequestVO,
//    ): ResponseDTO<*> {
//        authService.sendEmailVerificationCode(emailRequestVO)
//        return ResponseDTO.ok("메일 인증 코드가 보내졌습니다.")
//    }
// }