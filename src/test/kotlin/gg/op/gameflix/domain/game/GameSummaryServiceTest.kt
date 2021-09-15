package gg.op.gameflix.domain.game

import io.mockk.called
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
    fun `when findGameSummariesBySlugAndStore found summary expect not to find in gameRepository`(
        @MockK slugToFind: GameSlug, @MockK summaryFound: GameSummary) {
        every { summaryRepository.findFirstBySlugAndStore(slugToFind, Store.STEAM) } returns summaryFound

        service.findGameSummariesBySlugsAndStore(listOf(slugToFind), Store.STEAM)

        verify { gameRepository wasNot called }
    }

    @Test
    fun `when findGameSummariesBySlugAndStore not found summary expect to find in gameRepository`(
        @MockK slugNotExists: GameSlug) {
        every { summaryRepository.findFirstBySlugAndStore(slugNotExists, Store.STEAM) } returns null

        runCatching { service.findGameSummariesBySlugsAndStore(listOf(slugNotExists), Store.STEAM) }

        verify { gameRepository.findAllGameSummariesBySlugs(match { it.contains(slugNotExists) }) }
    }

    @Test
    fun `when findGameSummariesBySlugAndStore expect to find only not found summary in gameRepository`(
        @MockK slugNotExists: GameSlug, @MockK slugExists: GameSlug,@MockK summaryFound: GameSummary) {
        every { summaryRepository.findFirstBySlugAndStore(slugNotExists, Store.STEAM) } returns null
        every { summaryRepository.findFirstBySlugAndStore(slugExists, Store.STEAM) } returns summaryFound

        runCatching { service.findGameSummariesBySlugsAndStore(listOf(slugNotExists, slugExists), Store.STEAM) }

        verify { gameRepository.findAllGameSummariesBySlugs(match { it.contains(slugNotExists) and !it.contains(slugExists)}) }
    }

    @Test
    fun `when summary found in repository expect return found value`(
        @MockK gameSlug: GameSlug, @MockK gameSummary: GameSummary) {
        every { summaryRepository.findFirstBySlugAndStore(gameSlug) } returns gameSummary

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
        every { summaryRepository.findFirstBySlugAndStore(gameSlug) } returns null
        every { gameRepository.findFirstGameBySlug(gameSlug) } returns null
    }

    private fun whenGameSlugFoundInGameRepository(gameSlug: GameSlug, gameFound: Game): Game {
        every { summaryRepository.findFirstBySlugAndStore(gameSlug ) } returns null
        every { gameRepository.findFirstGameBySlug(gameSlug) } returns gameFound
        return gameFound
    }
}
