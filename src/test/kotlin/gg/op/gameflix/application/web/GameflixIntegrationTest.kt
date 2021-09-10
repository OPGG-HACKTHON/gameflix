package gg.op.gameflix.application.web

import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_GAME_SLUG
import gg.op.gameflix.application.web.security.WithMockGoogleUser
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserGameService
import gg.op.gameflix.domain.user.UserRepository
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@TestInstance(PER_CLASS)
@AutoConfigureMockMvc
@SpringBootTest
internal class GameflixIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userGameService: UserGameService

    private lateinit var userSaved: User

    @BeforeAll
    fun initializeUser() {
        userSaved = userRepository.save(MOCK_USER)
        userGameService.addGameToUser(userSaved, MOCK_USER_GAME_SLUG)
    }

    @Test
    fun `when GET actuator health expect status ok`() {
        mockMvc.get("/actuator/health")
            .andExpect {
                status { isOk() }
                content { json("{\"status\":\"UP\"}") }
            }
    }

    @Test
    fun `when POST users without authentication expect status unauthorized`() {
        mockMvc.post("/users")
            .andExpect { status { isUnauthorized() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when POST users with authentication expect status created with UserModel`() {
        mockMvc.post("/users")
            .andExpect {
                status { isCreated() }
                match(userModelContentsWith(userSaved))
            }
    }

    @WithMockGoogleUser
    @Test
    fun `when GET users with authentication expect status ok with UserModel`() {
        mockMvc.get("/users/${userSaved.id}")
            .andExpect {
                status { isOk() }
                match(userModelContentsWith(userSaved))
            }
    }

    @WithMockGoogleUser
    @Test
    fun `when GET users-{id}-games expect status ok with MultipleGameSummaryModel`() {
        mockMvc.get("/users/${userSaved.id}/games")
            .andExpect {
                status { isOk() }
                match(multipleGameSummaryModelWith(userSaved.games))
            }
    }

    @Transactional
    @WithMockGoogleUser
    @Test
    fun `when POST users-{user-id}-games expect status created with GameSummaryModel ad add 1 game`() {
        val summaryExpected =
            GameSummary(GameSlug("Portal 2"), "https://images.igdb.com/igdb/image/upload/t_cover_big/co1rs4.jpg")
        expectUserGamesHasSize(userSaved.games.size)

        postUsersGames(userSaved.id, summaryExpected.slug.slug)
            .andExpect {
                status { isCreated() }
                match(gameSummaryModelWith(summaryExpected))
            }

        expectUserGamesHasSize(userSaved.games.size + 1)
    }

    @Transactional
    @WithMockGoogleUser
    @Test
    fun `when DELETE user-{user-id}-games-{slug} expect status noContent and deleted 1 game`() {
        expectUserGamesHasSize(userSaved.games.size)

        mockMvc.delete("/users/${userSaved.id}/games/${MOCK_USER_GAME_SLUG.slug}")
            .andExpect { status { isNoContent() } }

        expectUserGamesHasSize(userSaved.games.size - 1)
    }

    @Test
    fun `when GET games-slug expect status ok with game model`() {
        mockMvc.get("/games/league-of-legends")
            .andExpect {
                status{ isOk() }
                content {
                    jsonPath("developer", `is`("riot-games"))
                    jsonPath("background", startsWith("https"))
                }
            }
    }

    private fun postUsersGames(userId: String, slug: String) =
        mockMvc.post("/users/$userId/games") {
            contentType = APPLICATION_JSON
            content = "{\"slug\": \"$slug\"}"
        }

    private fun expectUserGamesHasSize(sizeExpected: Int) =
        mockMvc.get("/users/${userSaved.id}/games")
            .andExpect {
                status { isOk() }
                match(MockMvcResultMatchers.jsonPath("$.games", hasSize<String>(sizeExpected)))
            }
}
