package com.bockerl.snailchat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

// @EnableFeignClients
@SpringBootApplication
@EnableScheduling
class SnailChatApplication

fun main(args: Array<String>) {
    runApplication<SnailChatApplication>(*args)
}