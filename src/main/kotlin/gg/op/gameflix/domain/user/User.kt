package gg.op.gameflix.domain.user

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameSummary
import javax.persistence.CascadeType.PERSIST
import javax.persistence.Entity
import javax.persistence.FetchType.EAGER
import javax.persistence.Id
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

@Entity
class User(
    @Id
    var id: String,
    var email: String,

    @JoinTable
    @ManyToMany(fetch = EAGER, cascade = [PERSIST])
    var games: MutableSet<GameSummary> = HashSet()
) {
    fun addGame(gameSummary: GameSummary) = games.add(gameSummary)

    fun findGameBySlug(gameSlug: GameSlug) = games.find { it.slug == gameSlug }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }
}


