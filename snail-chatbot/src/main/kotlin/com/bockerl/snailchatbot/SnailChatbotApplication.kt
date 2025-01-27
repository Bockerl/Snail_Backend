package com.bockerl.snailchatbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnailChatbotApplication

fun main(args: Array<String>) {
	runApplication<SnailChatbotApplication>(*args)
}
