/**
 * Copyright 2025 Bockerl
 * SPDX-License-Identifier: MIT
 */
package com.bockerl.snailmember.config

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
data class BlobConfig(
    @Value("\${spring.cloud.azure.storage.blob.connection-string}")
    val connectionString: String,
    @Value("\${spring.cloud.azure.storage.blob.container-name}")
    val containerName: String,
) {
    @Bean
    fun blobServiceClient(): BlobServiceClient {
        println("connectionString: " + connectionString)

        if (connectionString.isBlank()) {
            throw IllegalArgumentException("Azure Storage connection string is missing!")
        }

        return BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient()
    }

    @Bean
    fun blobContainerClient(): BlobContainerClient {
        val serviceClient = blobServiceClient()
        return serviceClient.getBlobContainerClient(containerName)
    }
}