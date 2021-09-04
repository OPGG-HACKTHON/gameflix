package gg.op.gameflix.application.web

import com.ninjasquad.springmockk.MockkBean
import gg.op.gameflix.application.web.security.SecurityTestConfiguration
import gg.op.gameflix.application.web.security.WithMockGoogleUser
import gg.op.gameflix.application.web.security.WithMockGoogleUserSecurityContextFactory.Companion.MOCK_USER_DEFAULT
import gg.op.gameflix.application.web.security.WithMockGoogleUserSecurityContextFactory.Companion.MOCK_USER_ID_DEFAULT
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.user.UserGameService
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
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
        every { userGameService.addGameToUser(MOCK_USER_DEFAULT, slugNotExists) } throws NoSuchElementException()

        mockMvc.post("/users/$MOCK_USER_ID_DEFAULT/games") {
            contentType = APPLICATION_JSON
            content = "{\"slug\": \"${slugNotExists.name}\"}" }
            .andExpect { status { isNotFound() } }
    }

    @WithMockGoogleUser
    @Test
    fun `when POST users-{id}-games with same user id expect status userGameService addGameToUser`() {
        val gameSlugExpected = GameSlug("League Of Legends")
        every { userGameService.addGameToUser(MOCK_USER_DEFAULT, gameSlugExpected) } returns GameSummary(gameSlugExpected, "cover-image")

        mockMvc.post("/users/$MOCK_USER_ID_DEFAULT/games") {
            contentType = APPLICATION_JSON
            content = "{\"slug\": \"League Of Legends\"}" }

        verify { userGameService.addGameToUser(MOCK_USER_DEFAULT, gameSlugExpected) }
    }

    @WithMockGoogleUser
    @Test
    fun `when POST users-{id}-games with same user id expect status created`() {
        val gameSlugExpected = GameSlug("League Of Legends")
        every { userGameService.addGameToUser(MOCK_USER_DEFAULT, gameSlugExpected) } returns GameSummary(gameSlugExpected, "cover-image")

        mockMvc.post("/users/$MOCK_USER_ID_DEFAULT/games") {
            contentType = APPLICATION_JSON
            content = "{\"slug\": \"League Of Legends\"}" }
            .andExpect { status { isCreated() } }
    }
}