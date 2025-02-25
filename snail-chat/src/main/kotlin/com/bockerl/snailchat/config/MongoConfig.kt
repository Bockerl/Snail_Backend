package com.bockerl.snailchat.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing

@Configuration
@EnableMongoAuditing // MongoDB 자동 생성 설정
class MongoConfig
