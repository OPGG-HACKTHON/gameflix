package gg.op.gameflix.infrastructure.igdb

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(IGDBConfigurationProperties::class)
@Configuration
class IGDBConfiguration {

    @Bean
    fun igdbClient(properties: IGDBConfigurationProperties) = IGDBWebClient(properties)
}

@ConstructorBinding
@ConfigurationProperties("infrastructure.igdb")
class IGDBConfigurationProperties(
    val baseUrl: String,
    val token: String,
    val clientId: String
)