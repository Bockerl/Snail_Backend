package com.bockerl.snailmember.member.command.config

import com.bockerl.snailmember.common.exception.CommonException
import com.bockerl.snailmember.common.exception.ErrorCode
import feign.Logger
import feign.RequestInterceptor
import feign.Response
import feign.codec.Encoder
import feign.form.FormEncoder
import feign.jackson.JacksonEncoder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.charset.Charset

@Configuration
class Oauth2FeignConfig {
    private val logger = KotlinLogging.logger {}

    // Feign Client loggin level 설정(가장 디테일한 full)
    @Bean
    fun feignLoggerLevel(): Logger.Level = Logger.Level.FULL

    @Bean
    fun requestInterceptor(): RequestInterceptor = RequestInterceptor { template ->
        logger.info { "Feign request URL: ${template.url()}" }
        logger.info { "Feign request method: ${template.method()}" }
        logger.info { "Feign request headers: ${template.headers()}" }
        // 바이트 배열을 문자열로 변환하여 로깅
        template.body()?.let { body ->
            val bodyContent = String(body, Charset.forName("UTF-8"))
            logger.info { "Feign request body content: $bodyContent" }
        }
    }

    // Content-Type 헤더를 'application/x-www-form-urlencoded'로 설정
    // key=value 형식으로 데이터 변환
    // URL 인코딩 처리
    // 필요한 경우 중첩된 객체 구조 처리
    @Bean
    fun feignEncoder(): Encoder = FormEncoder(JacksonEncoder())

    // HTTP 오류 응답을 자바/코틀린 예외로 변환
    @Bean
    fun errorDecoder(): ErrorDecoder = ErrorDecoder()

    class ErrorDecoder : feign.codec.ErrorDecoder {
        private val log = KotlinLogging.logger {}
        private val defaultErrorDecoder = feign.codec.ErrorDecoder.Default()

        override fun decode(methodKey: String, response: Response): Exception {
            if (response.status() >= 400) {
                log.error { "Oauth2 API error: ${response.body()?.asReader()?.readText()}" }
                return CommonException(ErrorCode.OAUTH2_API_CLIENT_ERROR)
            }
            return defaultErrorDecoder.decode(methodKey, response)
        }
    }
}
