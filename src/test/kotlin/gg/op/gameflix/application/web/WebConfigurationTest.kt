package gg.op.gameflix.application.web

import gg.op.gameflix.application.web.security.SecurityTestConfiguration
import gg.op.gameflix.domain.game.GameRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.options

@WebMvcTest(GameRestController::class)
@Import(WebConfiguration::class, SecurityTestConfiguration::class)
internal class WebConfigurationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var gameRepository: GameRepository

    @Test
    fun `when preflightRequest games expect cors headers`() {
        mockMvc.preflightRequest("/games", HttpMethod.GET)
            .andExpect {
                header {
                    string("Access-Control-Allow-Origin", "*")
                }
            }
    }

    private fun MockMvc.preflightRequest(uriTemplate: String, httpMethod: HttpMethod) =
        options(uriTemplate) {
            header("Origin", "http://localhost:8080")
            header("Access-Control-Request-Method", httpMethod.name)
        }
}