package com.tukio.tukioeventsservice.config

import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(FlywayProperties::class)
class FlywayConfig {

    @Bean
    fun flyway(dataSource: DataSource, flywayProperties: FlywayProperties): Flyway {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations(*flywayProperties.locations.toTypedArray())
            .baselineOnMigrate(flywayProperties.isBaselineOnMigrate)
            .load()

        flyway.migrate()
        return flyway
    }
}