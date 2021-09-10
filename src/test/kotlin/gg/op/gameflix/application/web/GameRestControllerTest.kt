package gg.op.gameflix.application.web

import com.ninjasquad.springmockk.MockkBean
import gg.op.gameflix.application.web.security.SecurityTestConfiguration
import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameDetail
import gg.op.gameflix.domain.game.GameRating
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.Genre
import gg.op.gameflix.domain.game.Platform
import io.mockk.every
import io.mockk.junit5.MockKExtension
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@ExtendWith(MockKExtension::class)
@Import(SecurityTestConfiguration::class)
@WebMvcTest
internal class GameRestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var gameRepository: GameRepository

    @Test
    fun `when GET games expect status ok`() {
        every { gameRepository.findAllGameSummaries(any()) } returns Page.empty()

        mockMvc.get("/games")
            .andExpect { status { isOk() } }
    }

    @Test
    fun `when GET games expect valid GameSummaryModel`() {
        every { gameRepository.findAllGameSummaries(any()) } returns PageImpl(listOf(GameSummary(GameSlug("Wow"), "https://google.com")))

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

    @Test
    fun `when GET gamesByName expect status ok`() {
        val nameToSearch = "Name to Search"
        every { gameRepository.findAllGameSummariesByName(nameToSearch, any()) } returns Page.empty()

        mockMvc.get("/games") { param("search", nameToSearch)}
            .andExpect { status { isOk() } }
    }

    @Test
    fun `when GET gamesBySlug with not exists slug expect return NotFound status`() {
        val slugNotExists = GameSlug("not-exists-slug")
        every { gameRepository.findFirstGameBySlug(slugNotExists) } returns null

        mockMvc.get("/games/${slugNotExists.slug}")
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `when GET gamesBySlug with exists slug expect return ok status`() {
        every { gameRepository.findFirstGameBySlug(gameSlugValid) } returns gameValid

        mockMvc.get("/games/${gameSlugValid.slug}")
            .andExpect { status { isOk() } }
    }

    private val gameSlugValid = GameSlug("game-slug-valid")

    private val gameValid = Game(
        GameSummary(gameSlugValid, "https://google.com"),
        GameDetail(releaseAt = 1010, updatedAt = 1020, url = "https://google.com", description = "game description",
            genres = setOf(Genre("mba")),
            platforms = setOf(Platform("win"), Platform("mac")),
            rating = GameRating(10.44f, 10),
            developer = "developer",
            background = "background"
        )
    )
}