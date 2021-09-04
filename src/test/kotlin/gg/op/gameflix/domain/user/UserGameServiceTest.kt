package gg.op.gameflix.domain.user

import gg.op.gameflix.domain.game.Game
import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.GameSummaryService
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class UserGameServiceTest {

    @InjectMockKs
    private lateinit var userGameService: UserGameService

    @RelaxedMockK
    private lateinit var summaryService: GameSummaryService

    @RelaxedMockK
    private lateinit var gameRepository: GameRepository

    @RelaxedMockK
    private lateinit var userRepository: UserRepository

    @Test
    fun `when gameSummary found in user expect return found summary`(
        @MockK user: User, @MockK slug: GameSlug, @MockK summary: GameSummary) {
        every { user.findGameBySlug(slug) } returns summary

        assertThat(userGameService.addGameToUser(user, slug)).isEqualTo(summary)
    }

    @Test
    fun `when gameSummary not found in user expect gameSummaryService to find GameSummary`(
        @RelaxedMockK user: User, @RelaxedMockK slug: GameSlug) {
        every { user.findGameBySlug(slug) } returns null
        every { summaryService.findGameSummaryBySlug(slug) } returns null

        runCatching { userGameService.addGameToUser(user, slug) }

        verify { summaryService.findGameSummaryBySlug(slug) }
    }

    @Test
    fun `when gameSummary not found in user expect NoSuchElementException`(
        @RelaxedMockK user: User, @RelaxedMockK slug: GameSlug) {
        every { user.findGameBySlug(slug) } returns null
        every { summaryService.findGameSummaryBySlug(slug) } returns null

        assertThatThrownBy { userGameService.addGameToUser(user, slug) }
            .isInstanceOf(NoSuchElementException::class.java)
    }

    @Test
    fun `when gameSummary found in gameSummaryService expect user to addGame and saved`(
        @RelaxedMockK user: User, @MockK slug: GameSlug, @MockK summary: GameSummary) {
        every { user.findGameBySlug(slug) } returns null
        every { summaryService.findGameSummaryBySlug(slug) } returns summary
        every { userRepository.save(user) } returns user

        userGameService.addGameToUser(user, slug)

        verifyOrder {
            user.addGame(summary)
            userRepository.save(user)
        }
    }

    @Test
    fun `when gameSummary found in gameSummaryService expect return found value`(
        @RelaxedMockK user: User, @MockK slug: GameSlug, @MockK summary: GameSummary) {
        every { user.findGameBySlug(slug) } returns null
        every { summaryService.findGameSummaryBySlug(slug) } returns summary
        every { userRepository.save(user) } returns user

        assertThat(userGameService.addGameToUser(user, slug)).isEqualTo(summary)
    }

    @Test
    fun `when findGameInUser with not exists game expect return null`(
        @MockK user: User, @MockK slugToFind: GameSlug) {
        every { user.games } answers { mutableSetOf() }

        assertThat(userGameService.findGameInUser(user, slugToFind)).isNull()
    }

    @Test
    fun `when findGameInUser with exists game expect gameRepository findGameBySlug`(
        @MockK user: User, @MockK slugToFind: GameSlug) {
        val summaryToFind = GameSummary(slugToFind, "cover-found")
        every { user.games } answers { mutableSetOf(summaryToFind) }

        userGameService.findGameInUser(user, slugToFind)

        verify { gameRepository.findGameBySlug(slugToFind) }
    }

    @Test
    fun `when findGameInUser with exists game expect return found game`(
        @MockK user: User, @MockK slugToFind: GameSlug, @MockK gameFound: Game) {
        val summaryToFind = GameSummary(slugToFind, "cover-found")
        every { user.games } answers { mutableSetOf(summaryToFind) }
        every { gameRepository.findGameBySlug(slugToFind) } returns gameFound

        assertThat(userGameService.findGameInUser(user, slugToFind)).isEqualTo(gameFound)
    }

    @Test
    fun `when deleteGameInUser with not exists expect userRepository not save user`(
        @MockK user: User, @MockK slugToFind: GameSlug) {
        every { user.games } answers { mutableSetOf() }

        runCatching { userGameService.deleteGameInUser(user, slugToFind) }

        verify { userRepository.save(user) wasNot Called }
    }

    @Test
    fun `when deleteGameInUser with not exists expect NoSuchElementException`(
        @MockK user: User, @RelaxedMockK slugToFind: GameSlug) {
        every { user.games } answers { mutableSetOf() }
        every { userRepository.save(user) } returns user

        assertThatThrownBy { userGameService.deleteGameInUser(user, slugToFind) }
            .isInstanceOf(NoSuchElementException::class.java)
    }

    @Test
    fun `when deleteGameInUser with exists slug expect userRepository save user`(
        @MockK user: User, @RelaxedMockK slugToFind: GameSlug) {
        every { user.games } answers { mutableSetOf(GameSummary(slugToFind, "cover")) }
        every { userRepository.save(user) } returns user

        runCatching { userGameService.deleteGameInUser(user, slugToFind) }

        verify { userRepository.save(user) }
    }
}