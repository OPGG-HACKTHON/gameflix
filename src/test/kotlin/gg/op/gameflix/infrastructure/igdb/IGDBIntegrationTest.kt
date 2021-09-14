package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.domain.game.GameRepository
import gg.op.gameflix.domain.game.GameSlug
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [IGDBConfiguration::class], initializers = [ConfigDataApplicationContextInitializer::class])
internal class IGDBIntegrationTest {

    @Autowired
    private lateinit var igdbClient: IGDBClient

    @Autowired
    private lateinit var gameRepository: GameRepository

    @Test
    fun `when igdbClient queryGetGames expect same size`() {
        val sizeExpected = 10
        assertThat(igdbClient.queryGetGames(PageRequest.of(0, sizeExpected))).hasSize(sizeExpected)
    }

    @Test
    fun `when igdbClient queryGetGames expect return valid cover id`() {
        val coverOfFirstSummary = igdbClient.queryGetGames(PageRequest.of(0, 1))
            .content.first().cover

        assertThat(coverOfFirstSummary).isPositive
    }

    @Test
    fun `when igdbClient queryGetCoverImages expect return valid IGDBCoverImage`(): Unit = runBlocking {
        val keyExpected = 99964
        val coverImageExpected = IGDBImage(keyExpected, "co254s")
        val idToCoverImage = igdbClient.queryGetCoverImages(setOf(keyExpected))

        assertThat(idToCoverImage).contains(coverImageExpected)
    }

    @Test
    fun `when igdbClient queryGetGameBySlug with invalid slug expect null`() {
        assertThat(igdbClient.queryGetGameBySlug(GameSlug("Invalid game name"))).isNull()
    }

    @Test
    fun `when igdbClient queryGetGameBySlug with valid slug expect not null`() {
        assertThat(igdbClient.queryGetGameBySlug(GameSlug("Portal 2"))).isNotNull
    }

    @Test
    fun `when igdbClient queryGetGenres expect return valid IGDBGenre`(): Unit = runBlocking {
        val idExpected = 36
        val genreExpected = IGDBGenre(idExpected, "moba")

        assertThat(igdbClient.queryGetGenres(setOf(idExpected))).containsOnly(genreExpected)
    }

    @Test
    fun `when igdbClient queryGetPlatforms expect return valid IGDBPlatform`(): Unit = runBlocking {
        val platformsExpected = listOf(IGDBPlatform(6, "win"), IGDBPlatform(14, "mac"))

        assertThat(igdbClient.queryGetPlatforms(platformsExpected.map { it.id })).containsAll(platformsExpected)
    }

    @Test
    fun `when igdbClient queryGetGamesByName expect return not empty`() {
        assertThat(igdbClient.queryGetGamesByName("League of", PageRequest.of(0, 10))).isNotEmpty
    }

    @Test
    fun `when igdbClient queryGetDeveloperByInvolvedCompanies expect valid IGDBCompany`(): Unit = runBlocking {
        val companyExpected = IGDBCompany(41, "riot-games")

        assertThat(igdbClient.queryGetDeveloperByInvolvedCompanies(listOf(8068, 8087, 8088))).isEqualTo(companyExpected)
    }

    @Test
    fun `when igdbClient queryGetScreenShots expect valid IGDBImage`(): Unit = runBlocking  {
        val imageExpected = IGDBImage(34387, "aujhj4buodogepfhpovb")

        assertThat(igdbClient.queryGetScreenShots(listOf(34387, 34388, 34389, 34390))).contains(imageExpected)
    }

    @Test
    fun `when igdbClient queryGetGamesBySlugs with valid slug expect not null`() {
        val gamesExpected = listOf(GameSlug("Portal 2"), GameSlug("League of legends"))

        assertThat(igdbClient.queryGetGamesBySlug(gamesExpected)).hasSize(2)
    }

    @Test
    fun `when GameRepository getAllGames return expected count games`() {
        val numExpected = 3

        assertThat(gameRepository.findAllGameSummaries(PageRequest.of(0, numExpected))).hasSize(numExpected)
    }

    @Test
    fun `when GameRepository findGameBySlug expect not null`() {
        assertThat(gameRepository.findFirstGameBySlug(GameSlug("League of Legends"))).isNotNull
    }

    @Test
    fun `when GameRepository findGameByName expect not empty`() {
        assertThat(gameRepository.findAllGameSummariesByName("league o", PageRequest.of(0, 10))).isEmpty()
    }
}
