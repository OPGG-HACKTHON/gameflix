package gg.op.gameflix.application.web

import com.ninjasquad.springmockk.MockkBean
import gg.op.gameflix.application.web.security.SecurityTestConfiguration
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_GAMES
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_GAME_SLUG
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_ID
import gg.op.gameflix.application.web.security.WithMockGoogleUser
import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameDetail
import gg.op.gameflix.domain.game.GameRating
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.user.UserGameService
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(MockKExtension::class)
@Import(SecurityTestConfiguration::class)
@WebMvcTest(UserRestController::class)
internal class UserRestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userGameService: UserGameService

    @Test
    fun `when POST users without authentication expect status unauthorized`() {
        mockMvc.post("/users")
            .andExpect { status { isUnauthorized() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when POST users with authentication expect status created`() {
        mockMvc.post("/users")
            .andExpect { status { isCreated() } }
    }

    @WithMockGoogleUser(sub = "id-different")
    @Test
    fun `when POST users-{id}-games with different user id expect status forbidden`() {
        mockMvc.post("/users/user-id/games") {
            contentType = APPLICATION_JSON
            content = "{\"slug\": \"League Of Legends\"}" }
            .andExpect { status { isForbidden() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when POST users-{id}-games with not exists gameSlug expect notFound status`() {
        val slugNotExists = GameSlug("Slug not exists")
        every { userGameService.addGameToUser(MOCK_USER, slugNotExists) } throws NoSuchElementException()

        mockMvc.post("/users/$MOCK_USER_ID/games") {
            contentType = APPLICATION_JSON
            content = "{\"slug\": \"${slugNotExists.name}\"}" }
            .andExpect { status { isNotFound() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when POST users-{id}-games with same user id expect status userGameService addGameToUser`() {
        val gameSlugExpected = GameSlug("League Of Legends")
        every { userGameService.addGameToUser(MOCK_USER, gameSlugExpected) } returns GameSummary(gameSlugExpected, "cover-image")

        mockMvc.post("/users/$MOCK_USER_ID/games") {
            contentType = APPLICATION_JSON
            content = "{\"slug\": \"League Of Legends\"}" }

        verify { userGameService.addGameToUser(MOCK_USER, gameSlugExpected) }
    }

    @WithMockGoogleUser
    @Test
    fun `when POST users-{id}-games with same user id expect status created`() {
        val gameSlugExpected = GameSlug("League Of Legends")
        every { userGameService.addGameToUser(MOCK_USER, gameSlugExpected) } returns GameSummary(gameSlugExpected, "cover-image")

        mockMvc.post("/users/$MOCK_USER_ID/games") {
            contentType = APPLICATION_JSON
            content = "{\"slug\": \"League Of Legends\"}" }
            .andExpect { status { isCreated() } }
    }

    @Test
    fun `when GET users-{id}-games without authentication expect status unAuthorized`() {
        mockMvc.get("/users/$MOCK_USER_ID/games")
            .andExpect { status { isUnauthorized() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when GET users-{id}-games with different user id expect status forbidden`() {
        mockMvc.get("/users/id-different/games")
            .andExpect { status { isForbidden() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when GET users-{id}-games with id expect return gameSummary models`() {
        mockMvc.get("/users/$MOCK_USER_ID/games")
            .andExpect {
                status { isOk() }
                match { multipleGameSummaryModelWith(MOCK_USER_GAMES) } }
    }

    @WithMockGoogleUser
    @Test
    fun `when GET users-{id}-games-{slug} with different user id expect status forbidden`() {
        mockMvc.get("/users/id-different/games/game-slug-not-exist")
            .andExpect { status { isForbidden() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when GET users-{id}-games-{slug} with not exists slug expect status notFound`() {
        every { userGameService.findGameInUser(MOCK_USER, GameSlug("Slug not exists")) } returns null

        mockMvc.get("/users/$MOCK_USER_ID/games/slug-not-exists")
            .andExpect { status { isNotFound() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when GET users-{id}-games-{slug} with exists slug expect return found game`() {
        every { userGameService.findGameInUser(MOCK_USER, any()) } returns mockUserGame()

        mockMvc.get("/users/$MOCK_USER_ID/games/${MOCK_USER_GAME_SLUG.slug}")
            .andExpect {
                status { isOk() }
                match(gameModelWith(mockUserGame())) }
    }

    @WithMockGoogleUser
    @Test
    fun `when DELETE users-{id}-games-{slug} with not exists slug expect status notFound`() {
        every { userGameService.deleteGameInUser(MOCK_USER, MOCK_USER_GAMES.first().slug) } throws NoSuchElementException()

        mockMvc.delete("/users/$MOCK_USER_ID/games/${MOCK_USER_GAME_SLUG.slug}")
            .andExpect { status { isNotFound() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when DELETE users-{id}-games-{slug} with exists slug expect status noContent`() {
        every { userGameService.deleteGameInUser(MOCK_USER, MOCK_USER_GAMES.first().slug) } just Runs

        mockMvc.delete("/users/$MOCK_USER_ID/games/${MOCK_USER_GAME_SLUG.slug}")
            .andExpect { status { isNoContent() } }
    }

    private fun mockUserGame(): Game =
        Game(MOCK_USER_GAMES.first(),
            GameDetail(0, 0,
                "url", "description",
                emptySet(), emptySet(),
                GameRating(90.11f, 10)))
}