package gg.op.gameflix.domain.game

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class GameSummaryServiceTest {

    @InjectMockKs
    private lateinit var service: GameSummaryService

    @MockK
    private lateinit var gameRepository: GameRepository

    @MockK
    private lateinit var summaryRepository: GameSummaryRepository

    @Test
    fun `when summary found in repository expect return found value`(
        @MockK gameSlug: GameSlug, @MockK gameSummary: GameSummary) {
        every { summaryRepository.findFirstBySlug(gameSlug) } returns gameSummary

        assertThat(service.findGameSummaryBySlug(gameSlug)).isEqualTo(gameSummary)
    }

    @Test
    fun `when summary not found in summaryRepository expect to find in gameRepository`(@MockK gameSlug: GameSlug) {
        whenGameSlugNotFoundInNowhere(gameSlug)

        service.findGameSummaryBySlug(gameSlug)

        verify { gameRepository.findFirstGameBySlug(gameSlug) }
    }

    @Test
    fun `when summary not found in summaryRepository nor gameRepository expect return null`(@MockK gameSlug: GameSlug) {
        whenGameSlugNotFoundInNowhere(gameSlug)

        assertThat(service.findGameSummaryBySlug(gameSlug)).isNull()
    }

    @Test
    fun `when found in gameRepository expect save value`(
        @MockK gameSlug: GameSlug, @MockK game: Game, @MockK gameSummary: GameSummary) {
        whenGameSlugFoundInGameRepository(gameSlug, game)
            .let { gameFound -> every { gameFound.summary } answers { gameSummary } }
        every { summaryRepository.save(gameSummary) } returns gameSummary

        service.findGameSummaryBySlug(gameSlug)

        verify { summaryRepository.save(gameSummary) }
    }

    @Test
    fun `when found in gameRepository expect return saved value`(
        @MockK gameSlug: GameSlug, @MockK game: Game, @MockK summary: GameSummary, @MockK summarySaved: GameSummary) {
        whenGameSlugFoundInGameRepository(gameSlug, game)
            .let { gameFound -> every { gameFound.summary } answers { summary } }
        every { summaryRepository.save(summary) } returns summarySaved

        assertThat(service.findGameSummaryBySlug(gameSlug)).isEqualTo(summarySaved)
    }

    private fun whenGameSlugNotFoundInNowhere(gameSlug: GameSlug) {
        every { summaryRepository.findFirstBySlug(gameSlug) } returns null
        every { gameRepository.findFirstGameBySlug(gameSlug) } returns null
    }

    private fun whenGameSlugFoundInGameRepository(gameSlug: GameSlug, gameFound: Game): Game {
        every { summaryRepository.findFirstBySlug(gameSlug) } returns null
        every { gameRepository.findFirstGameBySlug(gameSlug) } returns gameFound
        return gameFound
    }
}
