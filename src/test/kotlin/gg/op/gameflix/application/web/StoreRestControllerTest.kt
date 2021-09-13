package gg.op.gameflix.application.web

import gg.op.gameflix.application.web.security.SecurityTestConfiguration
import gg.op.gameflix.domain.game.Store
import gg.op.gameflix.domain.game.toSlug
import io.mockk.junit5.MockKExtension
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@ExtendWith(MockKExtension::class)
@Import(SecurityTestConfiguration::class)
@WebMvcTest(StoreRestController::class)
internal class StoreRestControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `when GET stores expect status ok`() {
        mockMvc.get("/stores")
            .andExpect { status { isOk() } }
    }

    @Test
    fun `when GET stores-{id} with not exists store expect return not found`() {
        mockMvc.get("/stores/store-not-exists")
            .andExpect { status { isNotFound() } }
    }

    @EnumSource(Store::class)
    @ParameterizedTest
    fun `when GET stores-{id} with valid store expect return StoreModel`(store: Store) {
        mockMvc.get("/stores/${store.toString().toSlug()}")
            .andExpect {
                status { isOk() }
                content {
                    jsonPath("slug", `is`(store.name.toSlug()))
                    jsonPath("authentication", not(emptyString()))
                }
            }
    }
}