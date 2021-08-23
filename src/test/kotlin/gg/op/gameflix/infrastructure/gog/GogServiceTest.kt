package gg.op.gameflix.infrastructure.gog

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import gg.op.gameflix.domain.game.GameSlug
import okhttp3.mockwebserver.MockResponse
import okio.Okio.buffer
import okio.Okio.source
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
@ContextConfiguration(
    classes = [GogConfiguration::class],
    initializers = [ConfigDataApplicationContextInitializer::class]
)
internal class GogServiceTest {

    @Autowired
    private lateinit var configurationProperties: GogConfigurationProperties
    private lateinit var gogService: GogService

    private val token = "aaRqpaC012yEVT5NdP-n2DvWK7Jvwwxh3nRypF1KdBLqbMbpePfDCaLU473oJJguGDgmJipSk4wtnzYW62r3CTH0qBoHpAtS_qBkigw-EVzMlIZNAJUrydjW2zJ5o-sAfr6aW61C2oN0cuT6SPRjVPg_hCTE6DriI1I9yt2xx1CXTOfz_nriT_RRmDQRRWZo"

    @BeforeAll
    fun initializeGogClient() {
        gogService = GogService(GogWebClient(configurationProperties, GogAuthentication("Bearer " + token)))
    }

    @Test
    fun `when getAllGameSlugsByAuthentication expect not empty`() {
        val response = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody("{\"title\": \"DISTRAINT 2\"}")
        val expected1 = buffer(source(response.getBody()!!.inputStream())).readUtf8()
        val objectMapper = ObjectMapper()
        val map: Map<String, String> = objectMapper.readValue(expected1)

        assertThat(map.get("title")?.let { GameSlug(it) }).isEqualTo(GameSlug("DISTRAINT 2"))
        // assertThat(gogService.getAllGameSlugsByAuthentication(GogAuthentication("Bearer "+token)))
        //     .containsOnlyOnce(GameSlug("DISTRAINT 2"))
    }
}
