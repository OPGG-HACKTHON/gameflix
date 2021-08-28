package gg.op.gameflix.infrastructure.blizzard

import gg.op.gameflix.domain.game.GameSlug
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(MockitoExtension::class)
internal class BlizzardServiceTest {;

    private var configurationProperties: BlizzardConfigurationProperties = BlizzardConfigurationProperties("http://localhost:8080")

    @Mock
    private lateinit var blizzardClient: BlizzardClient
    private lateinit var blizzardService: BlizzardService

    private val accessToken: String = "KRsu18Jdvy0QRntK4QqvwGgFj1cg2cGmyi";

    @BeforeEach
    fun initializeInstance() {
        MockitoAnnotations.initMocks(this)
        //blizzardClient = BlizzardWebClient(configurationProperties)
        blizzardService = BlizzardService(blizzardClient)
    }

    @Test
    fun `when application starts expect blizzard configuration properties initialized`() {
        assertThat(configurationProperties.baseUrl).isNotBlank
    }

    @Test
    fun `when blizzardClient getAllGameSlugsByAuthentication expect not empty`() {
        val authentication = BlizzardAuthentication(accessToken)
        `when`(blizzardClient.queryGetGames(authentication)).thenReturn(
            mutableListOf(
                GameSlug("wow")
            )
        )
        assertThat(
            blizzardClient.queryGetGames(authentication)
        ).contains(GameSlug("wow"))
    }

    @Test
    fun `when blizzardService getAllGameSlugsByAuthentication expect not empty`() {
        val authentication = BlizzardAuthentication(accessToken)
        `when`(blizzardClient.queryGetGames(authentication)).thenReturn(
            mutableListOf(
                GameSlug("wow")
            )
        )
        println(blizzardClient.queryGetGames(authentication))
        assertThat(
            blizzardService.getAllGameSlugsByAuthentication(authentication)
        ).contains(GameSlug("wow"))
    }
}