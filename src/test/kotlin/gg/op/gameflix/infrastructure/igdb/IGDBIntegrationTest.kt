package gg.op.gameflix.infrastructure.igdb

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
    fun `when igdbClient queryGetCoverImages expect return valid IGDBCoverImage`() {
        val keyExpected = 99964
        val coverImageExpected = IGDBCoverImage("co254s")
        val idToCoverImage = igdbClient.queryGetCoverImages(setOf(keyExpected))

        assertThat(idToCoverImage).containsEntry(keyExpected, coverImageExpected)
    }
}
