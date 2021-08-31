package gg.op.gameflix.infrastructure.google

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(GoogleConfigurationProperties::class)
@Configuration
class GoogleConfiguration {

    @Bean
    fun googleService(googleClient: GoogleClient) = GoogleService(googleClient)

    @Bean
    fun googleClient(properties: GoogleConfigurationProperties) = GoogleWebClient(properties)
}

@ConstructorBinding
@ConfigurationProperties("infrastructure.google")
class GoogleConfigurationProperties(
    val baseUrl: String
)