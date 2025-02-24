package com.bockerl.snailmember.member.command.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class Oauth2LoginProperties(
    // 코드 요청 시, redirect uri(kakao)
    @Value("\${KAKAO_REDIRECT_URI}")
    val kakaoRedirectUri: String,
    // rest api key(kakao)
    @Value("\${KAKAO_CLIENT_ID}")
    val kakaoClientId: String,
    // 인증 보안을 위한 secret key(kakao)
    @Value("\${KAKAO_CLIENT_SECRET}")
    val kakaoClientSecret: String,
    // 코드 요청 시, redirect uri(google)
    @Value("\${GOOGLE_REDIRECT_URI}")
    val googleRedirectUri: String,
    // rest api key(google)
    @Value("\${GOOGLE_CLIENT_ID}")
    val googleClientId: String,
    // 인증 보안을 위한 secret key(google)
    @Value("\${GOOGLE_CLIENT_SECRET}")
    val googleClientSecret: String,
    // 코드 요청 시, redirect uri(line)
    @Value("\${LINE_REDIRECT_URI}")
    val lineRedirectUri: String,
    // rest api key(line)
    @Value("\${LINE_CLIENT_ID}")
    val lineClientId: String,
    // 인증 보안을 위한 secret key(line)
    @Value("\${LINE_CLIENT_SECRET}")
    val lineClientSecret: String,
)
