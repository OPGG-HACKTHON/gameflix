package gg.op.gameflix.application.web

import gg.op.gameflix.application.web.security.SecurityTestConfiguration
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER
import gg.op.gameflix.application.web.security.SecurityTestConfiguration.Companion.MOCK_USER_ID
import gg.op.gameflix.application.web.security.WithMockGoogleUser
import gg.op.gameflix.domain.user.UserRepository
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post

@ExtendWith(MockKExtension::class)
@Import(SecurityTestConfiguration::class)
@WebMvcTest(UserRestController::class)
internal class UserRestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

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

    @WithMockGoogleUser
    @Test
    fun `when DELETE users with authentication expect userRepository delete`() {
        every { userRepository.delete(MOCK_USER) } just runs
        mockMvc.delete("/users/$MOCK_USER_ID")

        verify { userRepository.delete(MOCK_USER) }
    }

    @WithMockGoogleUser
    @Test
    fun `when DELETE users with authentication expect status noContent`() {
        mockMvc.delete("/users/$MOCK_USER_ID")
            .andExpect { status { isNoContent() } }
    }
}