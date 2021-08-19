package gg.op.gameflix.infrastructure.blizzard

import gg.op.gameflix.domain.game.GameStoreAuthentication
import gg.op.gameflix.domain.game.GameStoreService

class BlizzardService(private val client: BlizzardClient) : GameStoreService {
    override fun getAllGameSlugsByAuthentication(authentication: GameStoreAuthentication)
        = client.queryGetGames(authentication as BlizzardAuthentication)

    override fun supports(authentication: GameStoreAuthentication)
        = authentication is BlizzardAuthentication
}

data class BlizzardAuthentication(val accessToken: String) : GameStoreAuthentication
