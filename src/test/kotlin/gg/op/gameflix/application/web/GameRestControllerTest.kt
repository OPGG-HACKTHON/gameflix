package gg.op.gameflix.application.web

import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.util.any
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.net.URI

@ExtendWith(MockitoExtension::class)
@WebMvcTest
internal class GameRestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var gameRepository: GameRepository

    @Test
    fun `when GET games expect status ok`() {
        `when`(gameRepository.getAllGames(any(Pageable::class.java))).thenReturn(Page.empty())

        mockMvc.get("/games")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `when GET games expect valid GameSummaryModel`() {
        `when`(gameRepository.getAllGames(any(Pageable::class.java)))
            .thenReturn(PageImpl(listOf(GameSummary(GameSlug("Wow"), URI.create("https://google.com")))))

        mockMvc.get("/games")
            .andExpect {
                content {
                    jsonPath("$.games").isNotEmpty
                    jsonPath("$.games[0].name", `is`("Wow"))
                    jsonPath("$.games[0].slug", `is`("wow"))
                    jsonPath("$.games[0].cover", startsWith("https://"))
                }
            }
    }
}