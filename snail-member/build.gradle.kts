/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
@file:Suppress("DEPRECATION")

import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("me.champeau.jmh") version "0.7.2"
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
    ignoreFailures.set(true)
}

// build.gradle.kts
allOpen {
    annotation("jakarta.persistence.Entity")
}

noArg {
    annotation("jakarta.persistence.Entity")
}
val springCloudAzureVersion by extra("5.19.0")

group = "com.bockerl"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

springBoot {
    mainClass.set("com.bockerl.snailmember.SnailMemberApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.springdoc:springdoc-openapi-starter-common:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4")
    implementation("com.azure.spring:spring-cloud-azure-starter-storage")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:3.0.4")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")
    "developmentOnly"("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.opencsv:opencsv:5.9")
    // env file
    implementation("me.paulschwarz:spring-dotenv:4.0.0")
    // kotlin - logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    // sms service(coolsms)
    implementation("net.nurigo:sdk:4.3.2")
    // redis 직렬화 설정
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    // hibernate(Json 저장을 위한)
    implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
    // redis sentinel 가용을 위한
    implementation("io.lettuce:lettuce-core") // Redis Sentinel 지원을 위해 Lettuce 클라이언트 사용
    // feign client - Jackson encoder/decoder
    implementation("io.github.openfeign:feign-jackson")
    // jwt 토큰 라이브러리 추가
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-impl
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    // https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-jackson
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    // mockk 추가
    testImplementation("io.mockk:mockk:1.13.16")
    // kotest 추가
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    // 성능 테스트를 위한 jmh 추가
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    // prometheus 추가
    implementation("io.micrometer:micrometer-registry-prometheus")
}
dependencyManagement {
    imports {
        mavenBom("com.azure.spring:spring-cloud-azure-dependencies:$springCloudAzureVersion")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
    }
}

jmh {
    warmupIterations.set(2)
    iterations.set(5)
    fork.set(1)
    timeOnIteration.set("1s")
    resultFormat.set("JSON")
    resultsFile.set(file("build/reports/jmh/results.json"))
    zip64.set(true)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}