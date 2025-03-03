package com.bockerl.snailmember.member.command.application.controller

import com.bockerl.snailmember.common.ResponseDTO
import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import com.bockerl.snailmember.member.command.application.mapper.AuthConverter
import com.bockerl.snailmember.member.command.application.service.GoogleOauth2Service
import com.bockerl.snailmember.member.command.application.service.KaKaoOauth2Service
import com.bockerl.snailmember.member.command.application.service.LineOauth2Service
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/oauth2")
class Oauth2Controller(
    private val kakaoOauth2Service: KaKaoOauth2Service,
    private val googleOauth2Service: GoogleOauth2Service,
    private val lineOauth2Service: LineOauth2Service,
    private val authConverter: AuthConverter,
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/kakao/code")
    fun kakaoLogin(
        @RequestParam(value = "code", required = false) code: String?,
        @RequestParam(value = "error", required = false) error: String?,
        @RequestParam(value = "error_description", required = false) errorDescription: String?,
        @RequestParam(value = "state", required = false) state: String?,
    ): ResponseDTO<*> =
        when {
            error != null -> {
                logger.error { "카카오 로그인 취소/에러 발생 - error: $error, description: $errorDescription" }
                ResponseDTO.ok("카카오 로그인 취소")
            }

            code != null -> {
                logger.info { "카카오 로그인 시작 - code: $code" }
                val kakaoTokenDTO = kakaoOauth2Service.kakaoLogin(code)
                val kakaoTokenVO = authConverter.loginDTOToVO(kakaoTokenDTO)
                // jwt 토근과 회원 vo 객체에 발행할 예정
                ResponseDTO.ok(kakaoTokenVO)
            }

            else -> {
                logger.error { "카카오 로그인 콜백 - code, error 모두 null" }
                throw CommonException(ErrorCode.KAKAO_AUTH_ERROR)
            }
        }

    @GetMapping("/google/code")
    fun googleLogin(
        @RequestParam(value = "code", required = false) code: String?,
        @RequestParam(value = "error", required = false) error: String?,
        @RequestParam(value = "error_description", required = false) errorDescription: String?,
        @RequestParam(value = "state", required = false) state: String?,
    ): ResponseDTO<*> =
        when {
            error != null -> {
                logger.error { "구글 로그인 취소/에러 발생 - error: $error, description: $errorDescription" }
                ResponseDTO.ok("구글 로그인 취소")
            }

            code != null -> {
                logger.info { "구글 로그인 시작 - code: $code" }
                val googleToken = googleOauth2Service.googleLogin(code)
                // jwt 토큰과 회원 vo 객체 발행할 예정
                ResponseDTO.ok(googleToken)
            }

            else -> {
                logger.error { "구글 로그인 콜백 - code, error 모두 null" }
                throw CommonException(ErrorCode.GOOGLE_AUTH_ERROR)
            }
        }

    @GetMapping("/line/code")
    fun lineLogin(
        @RequestParam(value = "code", required = false) code: String?,
        @RequestParam(value = "error", required = false) error: String?,
        @RequestParam(value = "error_description", required = false) errorDescription: String?,
        @RequestParam(value = "state", required = false) state: String?,
    ): ResponseDTO<*> =
        when {
            error != null -> {
                logger.error { "라인 로그인 취소/에러 발생 - error: $error, description: $errorDescription" }
                ResponseDTO.ok("라인 로그인 취소")
            }

            code != null -> {
                logger.info { "라인 로그인 시작 - code: $code" }
                val lineToken = lineOauth2Service.lineLogin(code)
                // jwt 토근과 회원 vo 객체에 발행할 예정
                ResponseDTO.ok(lineToken)
            }

            else -> {
                logger.error { "라인 로그인 콜백 - code, error 모두 null" }
                throw CommonException(ErrorCode.KAKAO_AUTH_ERROR)
            }
        }
}