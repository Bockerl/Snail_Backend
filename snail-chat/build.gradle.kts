/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
@file:Suppress("DEPRECATION")

import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
//    kotlin("plugin.jpa") version "2.1.10"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

ktlint {
    version.set("1.5.0")
    verbose.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(ReporterType.JSON)
    }
    filter {
        exclude("**/*.kts")
    }
    disabledRules.set(setOf("header")) // HEADER_KEYWORD 규칙 비활성화
}

// build.gradle.kts
// allOpen {
//    annotation("jakarta.persistence.Entity")
// }
//
// noArg {
//    annotation("jakarta.persistence.Entity")
// }
val springCloudAzureVersion by extra("5.19.0")

group = "com.bockerl"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.springdoc:springdoc-openapi-starter-common:2.3.0")
//    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.azure.spring:spring-cloud-azure-starter-storage")
//    implementation("org.springframework.boot:spring-boot-starter-jdbc")
//    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.springframework.security:spring-security-test")

    "developmentOnly"("org.springframework.boot:spring-boot-devtools")
//    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // csv 파일 처리
    implementation("com.opencsv:opencsv:5.9")

    // env file
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    // kotlin - logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    // redis 직렬화 설정
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // hibernate(Json 저장을 위한)
//    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")

    // redis sentinel 가용을 위한
//    implementation("io.lettuce:lettuce-core") // Redis Sentinel 지원을 위해 Lettuce 클라이언트 사용

    // websocket 설정
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // kafka 설정
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-clients:2.5.0")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // netty 설정
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.117.Final")
}
dependencyManagement {
    imports {
        mavenBom("com.azure.spring:spring-cloud-azure-dependencies:$springCloudAzureVersion")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}