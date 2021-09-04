package gg.op.gameflix.domain.user

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.GameSummaryService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class UserGameServiceTest {

    @InjectMockKs
    private lateinit var userGameService: UserGameService

    @RelaxedMockK
    private lateinit var summaryService: GameSummaryService

    @Test
    fun `when gameSummary found in user expect return found summary`(
        @MockK user: User, @MockK slug: GameSlug, @MockK summary: GameSummary) {
        every { user.findGameBySlug(slug) } returns summary

        assertThat(userGameService.addGameToUser(user, slug)).isEqualTo(summary)
    }

    @Test
    fun `when gameSummary not found in user expect gameSummaryService to find GameSummary`(
        @RelaxedMockK user: User, @MockK slug: GameSlug) {
        every { user.findGameBySlug(slug) } returns null

        userGameService.addGameToUser(user, slug)

        verify { summaryService.findGameSummaryBySlug(slug) }
    }

    @Test
    fun `when gameSummary found in gameSummaryService expect user to addGame`(
        @RelaxedMockK user: User, @MockK slug: GameSlug, @MockK summary: GameSummary) {
        every { user.findGameBySlug(slug) } returns null
        every { summaryService.findGameSummaryBySlug(slug) } returns summary

        userGameService.addGameToUser(user, slug)

        verify { user.addGame(summary) }
    }

    @Test
    fun `when gameSummary found in gameSummaryService expect return found value`(
        @RelaxedMockK user: User, @MockK slug: GameSlug, @MockK summary: GameSummary) {
        every { user.findGameBySlug(slug) } returns null
        every { summaryService.findGameSummaryBySlug(slug) } returns summary

        assertThat(userGameService.addGameToUser(user, slug)).isEqualTo(summary)
    }
}