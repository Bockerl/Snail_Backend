package com.bockerl.snailchat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnailChatApplication

fun main(args: Array<String>) {
	runApplication<SnailChatApplication>(*args)
}
