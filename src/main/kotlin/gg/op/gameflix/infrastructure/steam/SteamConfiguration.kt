package gg.op.gameflix.infrastructure.steam

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(SteamConfigurationProperties::class)
@Configuration
internal class SteamConfiguration

@ConstructorBinding
@ConfigurationProperties("infrastructure.steam")
class SteamConfigurationProperties(
    val baseUrl: String,
    val apiKey: String
)