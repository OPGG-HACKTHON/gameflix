package gg.op.gameflix.application.web

import com.ninjasquad.springmockk.MockkBean
import gg.op.gameflix.application.web.security.SecurityTestConfiguration
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_GAMES
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_ID
import gg.op.gameflix.application.web.security.WithMockGoogleUser
import gg.op.gameflix.domain.game.UserStoreService
import gg.op.gameflix.domain.game.toSlug
import io.mockk.junit5.MockKExtension
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExtendWith(MockKExtension::class)
@Import(SecurityTestConfiguration::class)
@WebMvcTest(UserStoreRestController::class)
internal class UserStoreRestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userStoreService: UserStoreService

    private val MOCK_USER_STORE_SLUG =
        MOCK_USER_GAMES.map { it.store }
            .first().toString().toSlug()

    @WithMockGoogleUser
    @Test
    fun `when get users-{user-id}-stores expect multiple store model`() {
        mockMvc.get("/users/$MOCK_USER_ID/stores")
            .andExpect {
                status { isOk() }
                content { jsonPath("stores", hasSize<String>(2)) }
            }
    }

    @WithMockGoogleUser
    @Test
    fun `when get users-{user-id}-stores-{store-slug} with not exists store expect notFound status`() {
        mockMvc.get("/users/$MOCK_USER_ID/stores/store-not-exists")
            .andExpect {
                status { isNotFound() }
            }
    }

    @WithMockGoogleUser
    @Test
    fun `when get users-{user-id}-stores-{store-slug} with exists store expect StoreModel`() {
        mockMvc.get("/users/$MOCK_USER_ID/stores/$MOCK_USER_STORE_SLUG")
            .andExpect {
                status { isOk() }
                content { jsonPath("slug", `is`(MOCK_USER_STORE_SLUG)) }
            }
    }

    @WithMockGoogleUser
    @Test
    fun `when get users-{user-id}-stores-{store-slug}-games with not exists store expect return empty games field`() {
        mockMvc.get("/users/$MOCK_USER_ID/stores/store-not-exists/games")
            .andExpect {
                status { isOk() }
                content { jsonPath("games", hasSize<String>(0)) }
            }
    }

    @WithMockGoogleUser
    @Test
    fun `when get users-{user-id}-stores-{store-slug}-games with exists store return not empty`() {
        mockMvc.get("/users/$MOCK_USER_ID/stores/$MOCK_USER_STORE_SLUG/games")
            .andExpect {
                status { isOk() }
                content { jsonPath("games", hasSize<String>(greaterThan(0))) }
            }
    }
}