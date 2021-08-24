package gg.op.gameflix.infrastructure.gog

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [GogConfiguration::class], initializers = [ConfigDataApplicationContextInitializer::class])
internal class GogConfigurationTest {

    @Autowired
    private lateinit var configurationProperties: GogConfigurationProperties

    @Test
    fun `when application starts expect gog configuration properties initialized`() {
        Assertions.assertThat(configurationProperties.baseUrl).isNotBlank
    }
}