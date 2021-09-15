package gg.op.gameflix.domain.user

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import gg.op.gameflix.domain.game.GameSummaryRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@TestInstance(PER_CLASS)
@DataJpaTest
internal class GameSummaryRepositoryTest {

    private lateinit var userSaved: User

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var gameSummaryRepository: GameSummaryRepository

    @BeforeAll
    fun initializeUser() {
        userSaved = userRepository.save(User("userForTest", "userForTest@email.com"))
    }

    @Test
    fun `when findFirstBySlug with exists slug expect return saved summary`() {
        val slugToSearch = GameSlug("Slug")
        val summarySaved = GameSummary(slugToSearch, "https://google.com", 0, "")
        gameSummaryRepository.save(summarySaved)

        assertThat(gameSummaryRepository.findFirstBySlugAndStore(slugToSearch, null)).isEqualTo(summarySaved)
    }
}
