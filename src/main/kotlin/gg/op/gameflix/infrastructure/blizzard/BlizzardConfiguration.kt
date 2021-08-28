package gg.op.gameflix.infrastructure.blizzard

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(BlizzardConfigurationProperties::class)
@Configuration
interface BlizzardConfiguration

@ConstructorBinding
@ConfigurationProperties("infrastructure.blizzard")
class BlizzardConfigurationProperties(
    val baseUrl: String
)