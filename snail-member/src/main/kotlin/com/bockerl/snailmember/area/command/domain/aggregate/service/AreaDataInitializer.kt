package com.bockerl.snailmember.area.command.domain.aggregate.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AreaDataInitializer(
    private val areaApiService: AreaApiService,
) : ApplicationRunner {
    private val logger = KotlinLogging.logger {}

    @Transactional
    override fun run(args: ApplicationArguments) {
        try {
            if (areaApiService.isDatabaseEmpty()) {
                logger.info("지역 데이터베이스가 비어있습니다. API에서 데이터를 가져옵니다.")
                areaApiService.fetchApi()
                logger.info("지역 데이터 초기화가 완료되었습니다.")
            } else {
                logger.info("지역 데이터가 이미 존재합니다. 초기화를 건너뜁니다.")
            }
        } catch (e: Exception) {
            logger.error("지역 데이터 초기화 중 오류가 발생했습니다: ${e.message}", e)
            // 애플리케이션 시작에 필수적인 데이터라면 예외를 다시 던져서 시작을 중단할 수 있습니다.
            // throw e
        }
    }
}