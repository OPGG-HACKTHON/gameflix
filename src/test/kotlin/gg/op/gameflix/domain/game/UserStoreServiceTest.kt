package gg.op.gameflix.domain.game

import gg.op.gameflix.domain.user.User
import gg.op.gameflix.domain.user.UserGameService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(PER_CLASS)
@ExtendWith(MockKExtension::class)
internal class UserStoreServiceTest {

    private lateinit var userStoreService: UserStoreService

    @RelaxedMockK
    private lateinit var mockStoreService1: GameStoreService
    @RelaxedMockK
    private lateinit var mockStoreService2: GameStoreService
    @RelaxedMockK
    private lateinit var userGameService: UserGameService

    @BeforeAll
    fun initializeUserStoreService() {
        userStoreService = UserStoreService(listOf(mockStoreService1, mockStoreService2), userGameService)
    }

    @Test
    fun `when connectUserWithStore store not found expect IllegalStateException`(
        @MockK user: User, @MockK authentication: GameStoreAuthentication) {

        assertThatThrownBy { userStoreService.connectUserWithStore(user, authentication) }
            .isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `when connectUserWithStore store found expect getAllGameSlugByAuthentication`(
        @MockK user: User, @MockK authentication: GameStoreAuthentication) {
        every { mockStoreService1.supports(authentication) } returns true

        userStoreService.connectUserWithStore(user, authentication)

        verify { mockStoreService1.getAllGameSlugsByAuthentication(authentication) }
    }

    @Test
    fun `when connectUserWithStore store found expect userGameService addAllGamesToUserStore`(
        @MockK user: User, @MockK authentication: GameStoreAuthentication, @MockK slugFound: GameSlug) {
        every { mockStoreService1.supports(authentication) } returns true
        every { mockStoreService1.getAllGameSlugsByAuthentication(authentication) } returns listOf(slugFound)
        every { mockStoreService1.store } answers { Store.STEAM }

        userStoreService.connectUserWithStore(user, authentication)

        verify { userGameService.addAllGamesToUserStore(user, listOf(slugFound), Store.STEAM)}
    }

}