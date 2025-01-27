package com.bockerl.snaileurekaserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnailEurekaServerApplication

fun main(args: Array<String>) {
    runApplication<SnailEurekaServerApplication>(*args)
}
