package com.verobee.vergate.integration

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer

abstract class TestcontainersConfig {

    companion object {
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:17").apply {
            withDatabaseName("vergate_test")
            withUsername("test")
            withPassword("test")
            start()
        }

        @JvmStatic
        val redis = GenericContainer("valkey/valkey:8").apply {
            withExposedPorts(6379)
            start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.getMappedPort(6379) }
        }
    }
}
