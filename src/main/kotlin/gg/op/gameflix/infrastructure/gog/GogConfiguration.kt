package gg.op.gameflix.infrastructure.gog

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(GogConfigurationProperties::class)
@Configuration
internal class GogConfiguration

@ConstructorBinding
@ConfigurationProperties("infrastructure.gog")
class GogConfigurationProperties(
    val baseUrl: String
)