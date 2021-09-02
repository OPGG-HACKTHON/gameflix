package gg.op.gameflix.application.web

import gg.op.gameflix.application.web.security.BearerAuthenticationProvider
import gg.op.gameflix.application.web.security.WithMockGoogleUser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@TestInstance(PER_CLASS)
@WebMvcTest(UserRestController::class)
internal class UserRestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var bearerAuthenticationProvider: BearerAuthenticationProvider

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
}