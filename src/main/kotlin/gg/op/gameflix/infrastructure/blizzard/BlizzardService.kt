package gg.op.gameflix.infrastructure.blizzard

import gg.op.gameflix.domain.game.GameSlug
import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService
import gg.op.gameflix.domain.game.Store
import gg.op.gameflix.domain.game.Store.BLIZZARD

class BlizzardService(
    private val client: BlizzardClient
) : GameStoreService {
    override val store: Store = BLIZZARD

    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication): Collection<GameSlug>
        = client.queryGetGames(authentication as BlizzardAuthentication)
        .toMutableList()
        .apply {
            if (removeIf { slug -> slug == GameSlug("wow") }) {
                add(GameSlug("World of Warcraft"))
            }
        }

    override fun supports(authentication: GameStoreAuthentication)
        = authentication is BlizzardAuthentication
}

data class BlizzardAuthentication(val accessToken: String) : GameStoreAuthentication
