package com.bockerl.snailchat.infrastructure.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableMongoAuditing // MongoDB 자동 생성 설정
@EnableTransactionManagement // Kafka Outbox 패턴을 위한 Transactional 허용
class MongoConfig(
    @Value("\${spring.data.mongodb.uri}")
    private val connectionUri: String,
    @Value("\${spring.data.mongodb.database}")
    private val databaseName: String,
) : AbstractMongoClientConfiguration() {
    @Bean
    fun transactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager = MongoTransactionManager(dbFactory)

    @Bean
    fun mongoTemplate(): MongoTemplate = MongoTemplate(mongoClient(), databaseName)

    override fun mongoClient(): MongoClient {
        val connectionStringObj = ConnectionString(connectionUri)
        val mongoClientSettings =
            MongoClientSettings
                .builder()
                .applyConnectionString(connectionStringObj)
                .writeConcern(WriteConcern.ACKNOWLEDGED) // WriteConcern 기본 설정 옵션
                .readPreference(ReadPreference.secondaryPreferred()) //  ReadPreference Replica시 많이 설정하는 옵션
                .build()
        return MongoClients.create(mongoClientSettings)
    }

    override fun getDatabaseName(): String = databaseName
}