package gg.op.gameflix.infrastructure.igdb

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

@TestInstance(PER_CLASS)
@ExtendWith(MockKExtension::class)
internal class IGDBGameRepositoryTest(@MockK val igdbClient: IGDBClient) {

    private lateinit var igdbGameRepository: IGDBGameRepository

    @BeforeAll
    fun initializeGameRepository() {
        igdbGameRepository = IGDBGameRepository(igdbClient)
    }

    @Test
    fun `when igdbClient queryGetAllGames return empty expect return empty`() {
        every { igdbClient.queryGetGames(any()) } returns Page.empty()

        assertThat(igdbGameRepository.findAllGameSummaries(PageRequest.of(0,1))).isEmpty()
    }

    @Test
    fun `when igdbClient queryGet`() {
        val nameNotExists = "Name not exists"
        every { igdbClient.queryGetGamesByName(eq(nameNotExists), any()) } returns PageImpl(emptyList())

        assertThat(igdbGameRepository.findAllGameSummariesByName(nameNotExists, PageRequest.of(0, 10))).isEmpty()
    }
}