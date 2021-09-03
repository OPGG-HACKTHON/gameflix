package gg.op.gameflix.domain.game

import javax.persistence.Embeddable
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id

data class Game(
    val summary: GameSummary,
    val detail: GameDetail
)

@Entity
class GameSummary(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    var id: Long?,

    @Embedded
    var slug: GameSlug,
    var cover: String,
) {
    constructor(slug: GameSlug, cover: String): this(null, slug, cover)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameSummary

        if (slug != other.slug) return false
        if (cover != other.cover) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slug.hashCode()
        result = 31 * result + cover.hashCode()
        return result
    }
}

data class GameDetail(
    val releaseAt: Int,
    val updatedAt: Int,
    val url: String,
    val description: String,
    val genres: Set<Genre>,
    val platforms: Set<Platform>,
    val rating: GameRating
)

@Embeddable
class GameSlug(
    var name: String,
    var slug: String
) {
    constructor(name: String) : this(name, name.toSlug())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameSlug

        if (slug != other.slug) return false

        return true
    }

    override fun hashCode(): Int {
        return slug.hashCode()
    }
}

private fun String.toSlug() = lowercase()
    .replace("\n", " ")
    .replace("[^a-z\\d\\s]".toRegex(), " ")
    .split(" ")
    .joinToString("-")
    .replace("-+".toRegex(), "-")

data class Genre(val name: String)

data class Platform(val name: String)

data class GameRating(val rating: Float, val count: Int)
