package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.member.command.application.mapper.AuthConverter
import com.bockerl.snailmember.member.command.application.service.RegistrationService
import com.bockerl.snailmember.member.command.domain.vo.request.EmailRequestVO
import com.bockerl.snailmember.member.command.domain.vo.request.EmailVerifyRequestVO
import com.bockerl.snailmember.member.command.domain.vo.request.PhoneRequestVO
import net.nurigo.sdk.NurigoApp
import net.nurigo.sdk.message.model.Message
import net.nurigo.sdk.message.request.SingleMessageSendingRequest
import net.nurigo.sdk.message.response.SingleMessageSentResponse
import net.nurigo.sdk.message.service.DefaultMessageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/registration")
class RegistrationController(
    private val registrationService: RegistrationService,
    private val authConverter: AuthConverter,
    @Value("\${COOL_SMS_KEY}")
    private val coolKey: String,
    @Value("\${COOL_SMS_SECRET}")
    private val coolSecret: String,
    @Value("\${COOL_SMS_SENDER}")
    private val coolSender: String,
) {
    val messageService: DefaultMessageService =
        NurigoApp.initialize(coolKey, coolSecret, "https://api.coolsms.co.kr")

    @PostMapping("/initiation/email")
    fun postEmailRegistration(
        @RequestBody requestVO: EmailRequestVO,
    ): ResponseDTO<*> {
        val requestDTO = authConverter.emailRequestVOToDTO(requestVO)
        val redisKey = registrationService.initiateRegistration(requestDTO)
        return ResponseDTO.ok(redisKey)
    }

    @PostMapping("/verification/email")
    fun postEmailVerification(
        @RequestBody requestVO: EmailVerifyRequestVO,
    ): ResponseDTO<*> {
        val requestDTO = authConverter.emailVerifyRequestVOToDTO(requestVO)
        val redisKey = registrationService.verifyEmailCode(requestDTO)
        return ResponseDTO.ok(redisKey)
    }

    @PostMapping("/verification/email/refresh/{redisKey}")
    fun postEmailRefreshCode(
        @PathVariable redisKey: String,
    ): ResponseDTO<*> {
        registrationService.createEmailRefreshCode(redisKey)
        return ResponseDTO.ok("메일 인증 코드가 재발급되었습니다.")
    }

    @PostMapping("/initiation/phone")
    fun postPhoneRegistration(
        @RequestBody requestVO: PhoneRequestVO,
    ): SingleMessageSentResponse? {
        val requestDTO = authConverter.phoneRequestVOToDTO(requestVO)
        val verificationCode = registrationService.createPhoneVerificationCode(requestDTO)
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 함
        val message =
            Message(
                from = coolSender,
                to = requestVO.phoneNumber,
                text = "[Snail] 인증번호는 [$verificationCode]입니다.",
                country = "+82",
            )
        val response = messageService.sendOne(SingleMessageSendingRequest(message))
        return response
    }
}