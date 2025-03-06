@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.member.command.application.mapper.AuthConverter
import com.bockerl.snailmember.member.command.application.service.RegistrationService
import com.bockerl.snailmember.member.command.domain.vo.request.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import net.nurigo.sdk.NurigoApp
import net.nurigo.sdk.message.model.Message
import net.nurigo.sdk.message.request.SingleMessageSendingRequest
import net.nurigo.sdk.message.response.SingleMessageSentResponse
import net.nurigo.sdk.message.service.DefaultMessageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

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

    @Operation(
        summary = "이메일 회원 가입 시작",
        description = "닉네임, 이메일, 생년월일을 입력하여 회원가입을 시작합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "이메일 인증 코드 발송 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/initiation/email")
    fun postEmailRegistration(
        @RequestBody requestVO: EmailRequestVO,
    ): ResponseDTO<*> {
        val requestDTO = authConverter.emailRequestVOToDTO(requestVO)
        val redisId = registrationService.initiateRegistration(requestDTO)
        return ResponseDTO.ok(redisId)
    }

    @Operation(
        summary = "이메일 인증 코드 재발급",
        description = "이메일 인증 코드를 재발급 요청합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "이메일 인증 코드 재발급 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/verification/email/refresh/{redisId}")
    fun postEmailRefreshCode(
        @PathVariable redisId: String,
    ): ResponseDTO<*> {
        registrationService.createEmailRefreshCode(redisId)
        return ResponseDTO.ok("메일 인증 코드가 재발급되었습니다.")
    }

    @Operation(
        summary = "이메일 인증 시도",
        description = "이메일 인증 코드로 인증을 시도합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "이메일 인증 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/verification/email")
    fun postEmailVerification(
        @RequestBody requestVO: EmailVerifyRequestVO,
    ): ResponseDTO<*> {
        val requestDTO = authConverter.emailVerifyRequestVOToDTO(requestVO)
        val redisId = registrationService.verifyEmailCode(requestDTO)
        return ResponseDTO.ok(redisId)
    }

    @Operation(
        summary = "핸드폰 인증 코드 발급",
        description = "핸드폰 인증 코드를 발급",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "휴대폰 인증 코드 발급 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = SingleMessageSentResponse::class),
                    ),
                ],
            ),
        ],
    )
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

    @Operation(
        summary = "핸드폰 인증 코드 재발급",
        description = "핸드폰 인증 코드를 재발급",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "휴대폰 인증 코드 재발급 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = SingleMessageSentResponse::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/verification/phone/refresh")
    fun postPhoneRefreshCode(
        @RequestBody requestVO: PhoneRequestVO,
    ): SingleMessageSentResponse? {
        val requestDTO = authConverter.phoneRequestVOToDTO(requestVO)
        val refreshCode = registrationService.createPhoneRefreshCode(requestDTO)
        val message =
            Message(
                from = coolSender,
                to = requestVO.phoneNumber,
                text = "[Snail] 인증번호는 [$refreshCode]입니다.",
                country = "+82",
            )
        val response = messageService.sendOne(SingleMessageSendingRequest(message))
        return response
    }

    @Operation(
        summary = "핸드폰 인증 시도",
        description = "핸드폰 인증 코드로 인증을 시도합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "핸드폰 인증 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/verification/phone")
    fun postPhoneVerification(
        @RequestBody requestVO: PhoneVerifyRequestVO,
    ): ResponseDTO<*> {
        val requestDTO = authConverter.phoneVerifyRequestVOToDTO(requestVO)
        val redisId = registrationService.verifyPhoneCode(requestDTO)
        return ResponseDTO.ok(redisId)
    }

    @Operation(
        summary = "계정 비밀번호 입력",
        description = "새 계정의 비밀번호를 입력합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "비밀번호 입력 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/password")
    fun postPasswordRegistration(
        @RequestBody requestVO: PasswordRequestVO,
    ): ResponseDTO<*> {
        val requestDTO = authConverter.passwordRequestVOToDTO(requestVO)
        val redisId = registrationService.postPassword(requestDTO)
        return ResponseDTO.ok(redisId)
    }

    @Operation(
        summary = "활동지역 등록",
        description = "새 계정의 주 지역과 직장 지역을 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "활동지역 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ResponseDTO::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping("/activity_area")
    fun postActivityAreas(
        @RequestBody requestVO: ActivityAreaRegisterRequestVO,
    ): ResponseDTO<*> {
        val requestDTO = authConverter.activityAreaRegisterRequestVOToDTO(requestVO)
        registrationService.postActivityArea(requestDTO)
        return ResponseDTO.ok("회원가입에 성공했습니다.")
    }
}