package gg.op.gameflix.infrastructure.igdb

import gg.op.gameflix.util.any
import gg.op.gameflix.util.eq
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.`when`
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

@TestInstance(PER_CLASS)
@ExtendWith(MockitoExtension::class)
internal class IGDBGameRepositoryTest(@Mock val igdbClient: IGDBClient) {

    private lateinit var igdbGameRepository: IGDBGameRepository

    @BeforeAll
    fun initializeGameRepository() {
        igdbGameRepository = IGDBGameRepository(igdbClient)
    }

    @Test
    fun `when igdbClient queryGetAllGames return empty expect return empty`() {
        `when`(igdbClient.queryGetGames(any(Pageable::class.java))).thenReturn(Page.empty())

        assertThat(igdbGameRepository.getAllGames(PageRequest.of(0,1))).isEmpty()
    }

    @Test
    fun `when igdbClient queryGet`() {
        val nameNotExists = "Name not exists";
        `when`(igdbClient.queryGetGamesByName(eq(nameNotExists), any(Pageable::class.java))).thenReturn(PageImpl(emptyList()))

        assertThat(igdbGameRepository.findGamesByName(nameNotExists, PageRequest.of(0, 10))).isEmpty()
    }
}