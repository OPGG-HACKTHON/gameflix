package gg.op.gameflix.infrastructure.blizzard

import gg.op.gameflix.domain.game.GameSlug
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BlizzardConfiguration::class], initializers = [ConfigDataApplicationContextInitializer::class])
internal class BlizzardIntegrationTest {

    @Autowired
    private lateinit var configurationProperties: BlizzardConfigurationProperties

    private lateinit var blizzardClient: BlizzardClient
    private lateinit var blizzardService: BlizzardService

    @BeforeAll
    fun initializeInstance() {
        blizzardClient = BlizzardWebClient(configurationProperties)
        blizzardService = BlizzardService(blizzardClient)
    }

    @Test
    fun `when application starts expect blizzard configuration properties initialized`() {
        assertThat(configurationProperties.baseUrl).isNotBlank
    }

    @Test
    fun `when blizzardClient queryGetGames expect not empty`() {
        assertThat(blizzardClient.queryGetGames(BlizzardAuthentication("KR96SkRt7qvKuNuy5LYRwRUfMwakM66uR3")))
            .contains(GameSlug("wow"))
    }

    @Test
    fun `when steamService getAllGameSlugsByAuthentication expect not empty`() {
        assertThat(blizzardService.getAllGameSlugsByAuthentication(BlizzardAuthentication("KR96SkRt7qvKuNuy5LYRwRUfMwakM66uR3")))
            .containsOnlyOnce(GameSlug("wow"))
    }
}