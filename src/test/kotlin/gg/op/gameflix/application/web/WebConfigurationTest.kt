package gg.op.gameflix.application.web

import com.ninjasquad.springmockk.MockkBean
import gg.op.gameflix.application.web.security.SecurityConfigurationProperties
import gg.op.gameflix.application.web.security.SecurityTestConfiguration
import gg.op.gameflix.domain.game.GameRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.options

@WebMvcTest(GameRestController::class)
@Import(SecurityTestConfiguration::class)
internal class WebConfigurationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var securityConfigurationProperties: SecurityConfigurationProperties

    @MockkBean
    private lateinit var gameRepository: GameRepository

    @Test
    fun `when preflightRequest games expect cors headers`() {
        mockMvc.preflightRequest("/games", HttpMethod.GET)
            .andExpect {
                header {
                    string("Access-Control-Allow-Credentials", "true")
                    string("Access-Control-Allow-Origin", securityConfigurationProperties.allowedOrigins.first())
                }
            }
    }

    private fun MockMvc.preflightRequest(uriTemplate: String, httpMethod: HttpMethod) =
        options(uriTemplate) {
            header("Origin", securityConfigurationProperties.allowedOrigins.first())
            header("Access-Control-Request-Method", httpMethod.name)
        }
}