package com.bockerl.snaileurekagateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class SnailEurekaGatewayApplication

fun main(args: Array<String>) {
    runApplication<SnailEurekaGatewayApplication>(*args)
}