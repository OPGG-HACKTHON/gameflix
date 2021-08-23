package gg.op.gameflix.infrastructure.gog

import gg.op.gameflix.domain.game.GameSlug
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [GogConfiguration::class], initializers = [ConfigDataApplicationContextInitializer::class])
internal class GogServiceTest {

    @Autowired
    private lateinit var configurationProperties: GogConfigurationProperties
    private lateinit var gogService: GogService

    private val token = "aaRqpaC012yEVT5NdP-n2DvWK7Jvwwxh3nRypF1KdBLqbMbpePfDCaLU473oJJguGDgmJipSk4wtnzYW62r3CTH0qBoHpAtS_qBkigw-EVzMlIZNAJUrydjW2zJ5o-sAfr6aW61C2oN0cuT6SPRjVPg_hCTE6DriI1I9yt2xx1CXTOfz_nriT_RRmDQRRWZo"

    @BeforeAll
    fun initializeGogClient() {
        gogService = GogService(GogWebClient(configurationProperties,GogAuthentication("Bearer "+token)))
    }

    @Test
    fun `when getAllGameSlugsByAuthentication expect not empty`() {
        assertThat(gogService.getAllGameSlugsByAuthentication(GogAuthentication("Bearer "+token)))
            .containsOnlyOnce(GameSlug("DISTRAINT 2"))
    }
}
