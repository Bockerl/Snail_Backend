package com.bockerl.snailmember.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync
class AsyncConfig {
    @Bean(name = ["logTaskExecutor"])
    fun logTaskExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            // 기본으로 유지할 corePoolSize
            corePoolSize = 4
            // 최대 corePoolSize
            maxPoolSize = 20
            // queueSize
            queueCapacity = 500
            // Thread Prefix 이름
            setThreadNamePrefix("log-thread-")
            // queue가 가득 찼을 때의 정책(호출자가 직접 실행함으로써 예외 누락 방지)
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            // shutdown 시 queue에 남아있는 task 처리 정책(다 끝날 때까지 기다리기)
            setWaitForTasksToCompleteOnShutdown(true)
            // 모든 작업이 처리되기 힘든 경우 최대 대기 시간
            setAwaitTerminationSeconds(30)
            initialize()
        }
}